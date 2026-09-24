package com.example.data.model

data class AnimalQuestion(
    val id: String,
    val question: String,
    val animalEmoji: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String = "",
    val soundText: String = ""
)

data class PuzzlePackage(
    val id: String,
    val title: String,
    val description: String,
    val questions: List<AnimalQuestion>,
    val author: String = "Guru TK",
    val createdAt: Long = System.currentTimeMillis()
)

data class StudentSubmission(
    val id: Long = 0,
    val studentName: String,
    val puzzleTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val stars: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    val percentage: Int
        get() = if (totalQuestions > 0) ((score.toFloat() / totalQuestions) * 100).toInt() else 0
}
