package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzles")
data class PuzzleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val questionsJson: String,
    val author: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_submissions")
data class StudentSubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentName: String,
    val puzzleTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val stars: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
