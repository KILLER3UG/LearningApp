package com.selfproject.learningapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quiz: QuizEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quizzes: List<QuizEntity>)

    @Delete
    suspend fun delete(quiz: QuizEntity)

    @Query("SELECT * FROM quizzes WHERE documentUri = :documentUri ORDER BY timestamp DESC")
    fun getByDocument(documentUri: String): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE documentUri = :documentUri ORDER BY timestamp DESC")
    suspend fun getByDocumentSync(documentUri: String): List<QuizEntity>

    @Query("SELECT * FROM quizzes ORDER BY timestamp DESC")
    fun getAll(): Flow<List<QuizEntity>>

    @Query("DELETE FROM quizzes WHERE documentUri = :documentUri")
    suspend fun deleteByDocument(documentUri: String)
}
