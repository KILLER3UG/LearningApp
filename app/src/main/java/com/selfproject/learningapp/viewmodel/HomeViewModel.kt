package com.selfproject.learningapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfproject.learningapp.data.StudyRepository
import com.selfproject.learningapp.data.local.NoteAttachmentDao
import com.selfproject.learningapp.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Home screen ViewModel — manages note list, subject filtering, and swipe actions.
 * Issue 1: Note list with subject filter pills, swipe-to-pin/delete.
 */
class HomeViewModel(
    private val noteRepository: StudyRepository,
    private val noteAttachmentDao: NoteAttachmentDao
) : ViewModel() {

    private val _selectedSubject = MutableStateFlow<String?>(null)
    val selectedSubject: StateFlow<String?> = _selectedSubject

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    val subjects: StateFlow<List<String>> = noteRepository.allNotes
        .combine(_selectedSubject) { notes, _ -> notes }
        .map { notes -> notes.flatMap { it.subjects }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<Note>> = combine(
        noteRepository.allNotes,
        _selectedSubject
    ) { notes: List<Note>, subject: String? ->
        notes
            .filter { subject == null || it.subjects.contains(subject) }
            .sortedWith(compareByDescending<Note> { it.pinned }.thenByDescending { it.updatedAt })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectSubject(subject: String?) {
        _selectedSubject.value = subject
    }

    fun pinNote(note: Note) {
        viewModelScope.launch {
            noteRepository.updateNote(note.copy(pinned = !note.pinned))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteAttachmentDao.deleteAllForNote(note.id)
            noteRepository.deleteNote(note)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Re-fetch from repository
            _isRefreshing.value = false
        }
    }

    /**
     * Adds an attachment to a note.
     */
    suspend fun addAttachment(
        noteUri: String,
        fileName: String,
        fileType: String,
        fileSizeBytes: Long,
        mimeType: String,
        fileUri: String,
        cachedContent: String? = null
    ) {
        val attachment = com.selfproject.learningapp.data.local.NoteAttachmentEntity(
            id = UUID.randomUUID().toString(),
            noteUri = noteUri,
            fileName = fileName,
            fileType = fileType,
            fileSizeBytes = fileSizeBytes,
            mimeType = mimeType,
            fileUri = fileUri,
            uploadTimestamp = System.currentTimeMillis(),
            cachedContent = cachedContent
        )
        noteAttachmentDao.insertAttachment(attachment)
    }

    fun getAttachmentsForNote(noteUri: String) =
        noteAttachmentDao.getAttachmentsForNote(noteUri)
}