package com.example.data.repository

import com.example.data.local.EduDao
import com.example.data.local.PuzzleEntity
import com.example.data.local.StudentSubmissionEntity
import com.example.data.model.AnimalQuestion
import com.example.data.model.PuzzlePackage
import com.example.data.model.StudentSubmission
import com.example.util.QrCodeUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EduRepository(private val dao: EduDao) {

    val allPuzzles: Flow<List<PuzzlePackage>> = dao.getAllPuzzles().map { list ->
        list.mapNotNull { entity ->
            val pkg = QrCodeUtil.deserializePuzzle(entity.questionsJson)
            pkg?.copy(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                author = entity.author,
                createdAt = entity.createdAt
            )
        }
    }

    val allSubmissions: Flow<List<StudentSubmission>> = dao.getAllSubmissions().map { list ->
        list.map { entity ->
            StudentSubmission(
                id = entity.id,
                studentName = entity.studentName,
                puzzleTitle = entity.puzzleTitle,
                score = entity.score,
                totalQuestions = entity.totalQuestions,
                stars = entity.stars,
                timestamp = entity.timestamp,
                notes = entity.notes
            )
        }
    }

    suspend fun savePuzzle(puzzle: PuzzlePackage) {
        val json = QrCodeUtil.serializePuzzle(puzzle)
        dao.insertPuzzle(
            PuzzleEntity(
                id = puzzle.id,
                title = puzzle.title,
                description = puzzle.description,
                questionsJson = json,
                author = puzzle.author,
                createdAt = puzzle.createdAt
            )
        )
    }

    suspend fun deletePuzzle(id: String) {
        dao.deletePuzzle(id)
    }

    suspend fun saveSubmission(submission: StudentSubmission): Long {
        return dao.insertSubmission(
            StudentSubmissionEntity(
                id = submission.id,
                studentName = submission.studentName,
                puzzleTitle = submission.puzzleTitle,
                score = submission.score,
                totalQuestions = submission.totalQuestions,
                stars = submission.stars,
                timestamp = submission.timestamp,
                notes = submission.notes
            )
        )
    }

    suspend fun deleteSubmission(id: Long) {
        dao.deleteSubmission(id)
    }

    suspend fun clearAllSubmissions() {
        dao.clearAllSubmissions()
    }

    suspend fun checkAndSeedDefaults() {
        val sample = dao.getPuzzleById("preset_hewan_darat")
        if (sample == null) {
            val p1 = PuzzlePackage(
                id = "preset_hewan_darat",
                title = "Petualangan Sahabat Hewan 🦁",
                description = "Teka-teki logika hewan darat & makanannya untuk anak TK.",
                author = "Guru TK Pertiwi",
                questions = listOf(
                    AnimalQuestion(
                        id = "q1",
                        question = "Sapi yang lucu 🐮 suka makan apa ya?",
                        animalEmoji = "🐮",
                        options = listOf("Rumput Segar 🌿", "Ikan Goreng 🐟", "Permen Cokelat 🍫"),
                        correctIndex = 0,
                        explanation = "Sapi makan rumput hijau di padang rumput!",
                        soundText = "Moo~ Moo!"
                    ),
                    AnimalQuestion(
                        id = "q2",
                        question = "Hewan mana yang bersuara 'Guk guk!' dengan gembira?",
                        animalEmoji = "🐶",
                        options = listOf("Kucing Manis 🐱", "Anjing Sahabat 🐶", "Ayam Jago 🐓"),
                        correctIndex = 1,
                        explanation = "Anjing bersuara guk guk saat menyapa temannya!",
                        soundText = "Guk! Guk!"
                    ),
                    AnimalQuestion(
                        id = "q3",
                        question = "Hewan berbadan besar dan punya belalai panjang adalah...",
                        animalEmoji = "🐘",
                        options = listOf("Gajah Ramah 🐘", "Kelinci Mungil 🐰", "Monyet Cerdik 🐵"),
                        correctIndex = 0,
                        explanation = "Gajah memiliki belalai panjang untuk minum dan mandi!",
                        soundText = "Trrruuumph!"
                    ),
                    AnimalQuestion(
                        id = "q4",
                        question = "Siapa yang suka melompat-lompat dan makan wortel manis? 🥕",
                        animalEmoji = "🐰",
                        options = listOf("Kelinci Lucu 🐰", "Singa Raja Hutan 🦁", "Bebek Kuning 🦆"),
                        correctIndex = 0,
                        explanation = "Kelinci punya telinga panjang dan suka wortel!",
                        soundText = "Hop! Hop!"
                    )
                )
            )
            savePuzzle(p1)
        }
    }
}
