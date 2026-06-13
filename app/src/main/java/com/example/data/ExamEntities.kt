package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "exams")
@JsonClass(generateAdapter = true)
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val totalQuestions: Int,
    val timerSeconds: Long? = null, // null means untimed
    val sectionsJson: String, // List of ExamSection
    val answersJson: String, // Map<Int, Int> Maps question number -> correct option (1-4)
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attempts")
@JsonClass(generateAdapter = true)
data class ExamAttempt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val answersJson: String, // Map<Int, Int> Maps question number -> user's selected option (1-4, or 0/null for unanswered)
    val timeSpentSeconds: Long,
    val totalQuestions: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val unansweredCount: Int,
    val percentage: Double,
    val negativeScore: Double, // Negative score value
    val negativeScorePercentage: Double // Percentage with negative scoring applied
)

@JsonClass(generateAdapter = true)
data class ExamSection(
    val name: String,
    val startQuestion: Int,
    val endQuestion: Int
)
