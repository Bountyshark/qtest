package com.example.data

import kotlinx.coroutines.flow.Flow

class ExamRepository(private val examDao: ExamDao) {
    val allExams: Flow<List<Exam>> = examDao.getAllExams()

    suspend fun getExamById(id: Long): Exam? {
        return examDao.getExamById(id)
    }

    suspend fun insertExam(exam: Exam): Long {
        return examDao.insertExam(exam)
    }

    suspend fun deleteExam(examId: Long) {
        examDao.deleteExamById(examId)
        examDao.deleteAttemptsForExam(examId) // cascade delete attempts manually
    }

    suspend fun insertAttempt(attempt: ExamAttempt): Long {
        return examDao.insertAttempt(attempt)
    }

    fun getAttemptsForExam(examId: Long): Flow<List<ExamAttempt>> {
        return examDao.getAttemptsForExam(examId)
    }

    suspend fun deleteAttemptsForExam(examId: Long) {
        examDao.deleteAttemptsForExam(examId)
    }
    
    suspend fun deleteAttemptById(id: Long) {
        examDao.deleteAttemptById(id)
    }
}
