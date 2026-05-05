package com.selfproject.learningapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flashcard: FlashcardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flashcards: List<FlashcardEntity>)

    @Update
    suspend fun update(flashcard: FlashcardEntity)

    @Delete
    suspend fun delete(flashcard: FlashcardEntity)

    @Query("SELECT * FROM flashcards WHERE documentUri = :documentUri ORDER BY nextReview ASC")
    fun getByDocument(documentUri: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE documentUri = :documentUri ORDER BY nextReview ASC")
    suspend fun getByDocumentSync(documentUri: String): List<FlashcardEntity>

    @Query("SELECT * FROM flashcards WHERE nextReview <= :now ORDER BY nextReview ASC LIMIT :limit")
    suspend fun getDueForReview(now: Long = System.currentTimeMillis(), limit: Int = 20): List<FlashcardEntity>

    @Query("SELECT * FROM flashcards ORDER BY nextReview ASC")
    fun getAll(): Flow<List<FlashcardEntity>>

    @Query("DELETE FROM flashcards WHERE documentUri = :documentUri")
    suspend fun deleteByDocument(documentUri: String)
}
