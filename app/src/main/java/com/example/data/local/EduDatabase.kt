package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AnimalQuestion
import com.example.data.model.PuzzlePackage
import com.example.util.QrCodeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [PuzzleEntity::class, StudentSubmissionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EduDatabase : RoomDatabase() {
    abstract fun eduDao(): EduDao

    companion object {
        @Volatile
        private var INSTANCE: EduDatabase? = null

        fun getInstance(context: Context): EduDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduDatabase::class.java,
                    "edutk_database.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with default kindergarten animal puzzles
                        CoroutineScope(Dispatchers.IO).launch {
                            val defaultPuzzle1 = PuzzlePackage(
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

                            val defaultPuzzle2 = PuzzlePackage(
                                id = "preset_hewan_air_udara",
                                title = "Hewan Air & Langit 🐬",
                                description = "Mengenal hewan yang berenang dan terbang bebas.",
                                author = "Guru TK Pertiwi",
                                questions = listOf(
                                    AnimalQuestion(
                                        id = "qa1",
                                        question = "Hewan mana yang bisa bernapas dan berenang lincah di laut? 🌊",
                                        animalEmoji = "🐬",
                                        options = listOf("Lumba-lumba Ceria 🐬", "Kambing Gunung 🐐", "Burung Merpati 🕊️"),
                                        correctIndex = 0,
                                        explanation = "Lumba-lumba hidup di laut dan sangat pintar!",
                                        soundText = "Klik-klik!"
                                    ),
                                    AnimalQuestion(
                                        id = "qa2",
                                        question = "Burung yang suka bersuara 'Kuk-kuk' di malam hari adalah...",
                                        animalEmoji = "🦉",
                                        options = listOf("Ayam Jantan 🐓", "Burung Hantu 🦉", "Bebek 🦆"),
                                        correctIndex = 1,
                                        explanation = "Burung hantu matanya bulat dan bangun malam hari!",
                                        soundText = "Huuu-huuu!"
                                    ),
                                    AnimalQuestion(
                                        id = "qa3",
                                        question = "Hewan kecil penghasil madu manis yang suka bunga adalah...",
                                        animalEmoji = "🐝",
                                        options = listOf("Lebah Pekerja 🐝", "Laba-laba 🕷️", "Kupu-kupu 🦋"),
                                        correctIndex = 0,
                                        explanation = "Lebah mengumpulkan nektar bunga untuk membuat madu!",
                                        soundText = "Bzzzt-bzzzt!"
                                    )
                                )
                            )

                            val dao = getInstance(context).eduDao()
                            dao.insertPuzzle(
                                PuzzleEntity(
                                    id = defaultPuzzle1.id,
                                    title = defaultPuzzle1.title,
                                    description = defaultPuzzle1.description,
                                    questionsJson = QrCodeUtil.serializePuzzle(defaultPuzzle1),
                                    author = defaultPuzzle1.author,
                                    createdAt = defaultPuzzle1.createdAt
                                )
                            )
                            dao.insertPuzzle(
                                PuzzleEntity(
                                    id = defaultPuzzle2.id,
                                    title = defaultPuzzle2.title,
                                    description = defaultPuzzle2.description,
                                    questionsJson = QrCodeUtil.serializePuzzle(defaultPuzzle2),
                                    author = defaultPuzzle2.author,
                                    createdAt = defaultPuzzle2.createdAt
                                )
                            )

                            // Initial sample student submission so teacher dashboard has initial data
                            dao.insertSubmission(
                                StudentSubmissionEntity(
                                    studentName = "Aisyah (TK-B)",
                                    puzzleTitle = "Petualangan Sahabat Hewan 🦁",
                                    score = 4,
                                    totalQuestions = 4,
                                    stars = 3,
                                    timestamp = System.currentTimeMillis() - 3600000,
                                    notes = "Semua jawaban benar! Hebat sekali."
                                )
                            )
                            dao.insertSubmission(
                                StudentSubmissionEntity(
                                    studentName = "Budi Santoso (TK-B)",
                                    puzzleTitle = "Petualangan Sahabat Hewan 🦁",
                                    score = 3,
                                    totalQuestions = 4,
                                    stars = 2,
                                    timestamp = System.currentTimeMillis() - 7200000,
                                    notes = "Perlu latihan mengenal suara hewan."
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
