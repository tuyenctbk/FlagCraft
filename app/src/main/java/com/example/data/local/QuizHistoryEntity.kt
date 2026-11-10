package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_history")
data class QuizHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String, // "BLITZ", "PARTY", "FLASHCARD"
    val score: Int,
    val totalQuestions: Int,
    val accuracyPercentage: Int,
    val timestamp: Long = System.currentTimeMillis()
)
