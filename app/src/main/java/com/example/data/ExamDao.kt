package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY createdAt DESC")
    fun getAllExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    suspend fun getExamById(id: Long): Exam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: ExamAttempt): Long

    @Update
    suspend fun updateAttempt(attempt: ExamAttempt)

    @Query("SELECT * FROM attempts WHERE examId = :examId AND isDraft = 0 ORDER BY createdAt DESC")
    fun getAttemptsForExam(examId: Long): Flow<List<ExamAttempt>>

    @Query("SELECT * FROM attempts WHERE examId = :examId AND isDraft = 1 LIMIT 1")
    suspend fun getDraftAttempt(examId: Long): ExamAttempt?

    @Query("SELECT examId FROM attempts WHERE isDraft = 1")
    fun getDraftExamIds(): Flow<List<Long>>

    @Query("DELETE FROM attempts WHERE examId = :examId")
    suspend fun deleteAttemptsForExam(examId: Long)
    
    @Query("DELETE FROM attempts WHERE id = :id")
    suspend fun deleteAttemptById(id: Long)

    @Query("DELETE FROM attempts WHERE examId = :examId AND isDraft = 1")
    suspend fun deleteDraftAttempt(examId: Long)
}
