package com.selfproject.learningapp.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.selfproject.learningapp.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.selfproject.learningapp.model.Note

/**
 * Repository for study-related data: bookmarks, flashcards, quizzes, notes, attachments.
 */
class StudyRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val bookmarkDao = db.bookmarkDao()
    private val flashcardDao = db.flashcardDao()
    private val quizDao = db.quizDao()
    private val noteAttachmentDao = db.noteAttachmentDao()

    // Notes
    private val _allNotes = MutableStateFlow<List<com.selfproject.learningapp.model.Note>>(emptyList())
    val allNotes: StateFlow<List<com.selfproject.learningapp.model.Note>> = _allNotes

    suspend fun updateNote(note: com.selfproject.learningapp.model.Note) {
        // Update in-memory cache
        _allNotes.value = _allNotes.value.map { if (it.id == note.id) note else it }
    }

    suspend fun deleteNote(note: com.selfproject.learningapp.model.Note) {
        _allNotes.value = _allNotes.value.filter { it.id != note.id }
    }

    fun addOrUpdateNote(note: com.selfproject.learningapp.model.Note) {
        val existing = _allNotes.value.find { it.id == note.id }
        if (existing != null) {
            _allNotes.value = _allNotes.value.map { if (it.id == note.id) note else it }
        } else {
            _allNotes.value = _allNotes.value + note
        }
    }

    // Bookmarks
    fun getBookmarks(documentUri: String): Flow<List<BookmarkEntity>> =
        bookmarkDao.getByDocument(documentUri)

    fun getAllBookmarks(): Flow<List<BookmarkEntity>> =
        bookmarkDao.getAll()

    suspend fun addBookmark(bookmark: BookmarkEntity) =
        bookmarkDao.insert(bookmark)

    suspend fun removeBookmark(bookmark: BookmarkEntity) =
        bookmarkDao.delete(bookmark)

    suspend fun hasBookmark(documentUri: String, heading: String): Boolean {
        val bookmarks = bookmarkDao.getByDocumentSync(documentUri)
        return bookmarks.any { it.heading == heading }
    }

    // Flashcards
    fun getFlashcards(documentUri: String): Flow<List<FlashcardEntity>> =
        flashcardDao.getByDocument(documentUri)

    fun getAllFlashcards(): Flow<List<FlashcardEntity>> =
        flashcardDao.getAll()

    suspend fun addFlashcard(flashcard: FlashcardEntity) =
        flashcardDao.insert(flashcard)

    suspend fun addFlashcards(flashcards: List<FlashcardEntity>) =
        flashcardDao.insertAll(flashcards)

    suspend fun updateFlashcard(flashcard: FlashcardEntity) =
        flashcardDao.update(flashcard)

    suspend fun removeFlashcard(flashcard: FlashcardEntity) =
        flashcardDao.delete(flashcard)

    suspend fun getDueFlashcards(): List<FlashcardEntity> =
        flashcardDao.getDueForReview()

    // Quizzes
    fun getQuizzes(documentUri: String): Flow<List<QuizEntity>> =
        quizDao.getByDocument(documentUri)

    fun getAllQuizzes(): Flow<List<QuizEntity>> =
        quizDao.getAll()

    suspend fun addQuiz(quiz: QuizEntity) =
        quizDao.insert(quiz)

    suspend fun addQuizzes(quizzes: List<QuizEntity>) =
        quizDao.insertAll(quizzes)

    suspend fun removeQuiz(quiz: QuizEntity) =
        quizDao.delete(quiz)

    // Spaced repetition calculation (SM-2 simplified)
    suspend fun recordFlashcardResult(flashcard: FlashcardEntity, correct: Boolean) {
        val updated = if (correct) {
            val newRepetitions = flashcard.repetitions + 1
            val newInterval = when (newRepetitions) {
                1 -> 1L
                2 -> 3L
                else -> (flashcard.interval * flashcard.easeFactor).toLong()
            }
            flashcard.copy(
                interval = newInterval,
                repetitions = newRepetitions,
                nextReview = System.currentTimeMillis() + (newInterval * 86_400_000L),
                easeFactor = maxOf(1.3, flashcard.easeFactor - if (newRepetitions > 2) 0.0 else 0.0)
            )
        } else {
            flashcard.copy(
                interval = 1L,
                repetitions = 0,
                nextReview = System.currentTimeMillis() + 86_400_000L
            )
        }
        flashcardDao.update(updated)
    }
}
