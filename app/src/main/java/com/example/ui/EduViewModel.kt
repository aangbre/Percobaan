package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AnimalQuestion
import com.example.data.model.PuzzlePackage
import com.example.data.model.StudentSubmission
import com.example.data.repository.EduRepository
import com.example.util.QrCodeUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UserRole {
    STUDENT,
    TEACHER
}

enum class ScanPurpose {
    STUDENT_SCAN_PUZZLE,
    TEACHER_SCAN_RESULT
}

data class QrDisplayData(
    val title: String,
    val subtitle: String,
    val payload: String,
    val iconEmoji: String = "📱"
)

class EduViewModel(private val repository: EduRepository) : ViewModel() {

    val puzzles: StateFlow<List<PuzzlePackage>> = repository.allPuzzles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val submissions: StateFlow<List<StudentSubmission>> = repository.allSubmissions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentRole = MutableStateFlow(UserRole.STUDENT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // --- Student Quiz Flow State ---
    private val _studentName = MutableStateFlow("Siswa Pintar")
    val studentName: StateFlow<String> = _studentName.asStateFlow()

    private val _activePuzzle = MutableStateFlow<PuzzlePackage?>(null)
    val activePuzzle: StateFlow<PuzzlePackage?> = _activePuzzle.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedOption = MutableStateFlow<Int?>(null)
    val selectedOption: StateFlow<Int?> = _selectedOption.asStateFlow()

    private val _isAnswerSubmitted = MutableStateFlow(false)
    val isAnswerSubmitted: StateFlow<Boolean> = _isAnswerSubmitted.asStateFlow()

    private val _currentScore = MutableStateFlow(0)
    val currentScore: StateFlow<Int> = _currentScore.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    // --- Dialogs & Scanner State ---
    private val _qrDisplayData = MutableStateFlow<QrDisplayData?>(null)
    val qrDisplayData: StateFlow<QrDisplayData?> = _qrDisplayData.asStateFlow()

    private val _scannerPurpose = MutableStateFlow<ScanPurpose?>(null)
    val scannerPurpose: StateFlow<ScanPurpose?> = _scannerPurpose.asStateFlow()

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedDefaults()
        }
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    fun setStudentName(name: String) {
        if (name.isNotBlank()) {
            _studentName.value = name.trim()
        }
    }

    fun startPuzzle(puzzle: PuzzlePackage) {
        _activePuzzle.value = puzzle
        _currentQuestionIndex.value = 0
        _selectedOption.value = null
        _isAnswerSubmitted.value = false
        _currentScore.value = 0
        _isQuizFinished.value = false
    }

    fun resetActivePuzzle() {
        _activePuzzle.value = null
        _isQuizFinished.value = false
        _selectedOption.value = null
        _isAnswerSubmitted.value = false
    }

    fun selectOption(index: Int) {
        if (!_isAnswerSubmitted.value) {
            _selectedOption.value = index
        }
    }

    fun submitAnswer() {
        val puzzle = _activePuzzle.value ?: return
        val currentQ = puzzle.questions.getOrNull(_currentQuestionIndex.value) ?: return
        val chosen = _selectedOption.value ?: return

        _isAnswerSubmitted.value = true
        if (chosen == currentQ.correctIndex) {
            _currentScore.value += 1
        }
    }

    fun nextQuestion() {
        val puzzle = _activePuzzle.value ?: return
        if (_currentQuestionIndex.value + 1 < puzzle.questions.size) {
            _currentQuestionIndex.value += 1
            _selectedOption.value = null
            _isAnswerSubmitted.value = false
        } else {
            _isQuizFinished.value = true
        }
    }

    fun generateStudentResultQr() {
        val puzzle = _activePuzzle.value ?: return
        val total = puzzle.questions.size
        val score = _currentScore.value
        val stars = when {
            total == 0 -> 1
            score == total -> 3
            score >= (total / 2) -> 2
            else -> 1
        }
        val result = StudentSubmission(
            studentName = _studentName.value,
            puzzleTitle = puzzle.title,
            score = score,
            totalQuestions = total,
            stars = stars,
            timestamp = System.currentTimeMillis(),
            notes = "Dikerjakan di Dasbor Siswa TK"
        )
        val payload = QrCodeUtil.serializeStudentResult(result)
        _qrDisplayData.value = QrDisplayData(
            title = "QR Hasil Belajar Siswa 🌟",
            subtitle = "Tunjukkan QR ini ke Bu Guru untuk dicatat nilainya!",
            payload = payload,
            iconEmoji = "🎓"
        )
    }

    fun showPuzzleQr(puzzle: PuzzlePackage) {
        val payload = QrCodeUtil.serializePuzzle(puzzle)
        _qrDisplayData.value = QrDisplayData(
            title = "QR Teka-Teki: ${puzzle.title}",
            subtitle = "Siswa dapat memindai QR ini untuk membuka soal secara offline.",
            payload = payload,
            iconEmoji = "🦁"
        )
    }

    fun dismissQrDialog() {
        _qrDisplayData.value = null
    }

    fun openScanner(purpose: ScanPurpose) {
        _scannerPurpose.value = purpose
    }

    fun closeScanner() {
        _scannerPurpose.value = null
    }

    fun onQrScanned(rawContent: String) {
        val purpose = _scannerPurpose.value ?: return
        _scannerPurpose.value = null

        when (purpose) {
            ScanPurpose.TEACHER_SCAN_RESULT -> {
                val studentResult = QrCodeUtil.deserializeStudentResult(rawContent)
                if (studentResult != null) {
                    viewModelScope.launch {
                        repository.saveSubmission(studentResult)
                        _bannerMessage.value = "Berhasil mencatat nilai: ${studentResult.studentName} (${studentResult.score}/${studentResult.totalQuestions} ⭐)"
                    }
                } else {
                    _bannerMessage.value = "Format QR bukan data nilai siswa yang valid."
                }
            }
            ScanPurpose.STUDENT_SCAN_PUZZLE -> {
                val puzzle = QrCodeUtil.deserializePuzzle(rawContent)
                if (puzzle != null && puzzle.questions.isNotEmpty()) {
                    viewModelScope.launch {
                        repository.savePuzzle(puzzle)
                        startPuzzle(puzzle)
                        _bannerMessage.value = "Teka-Teki '${puzzle.title}' berhasil dibuka!"
                    }
                } else {
                    _bannerMessage.value = "Format QR bukan teka-teki TK yang valid."
                }
            }
        }
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    // Teacher actions
    fun createPuzzle(
        title: String,
        description: String,
        author: String,
        questions: List<AnimalQuestion>
    ) {
        val newPuzzle = PuzzlePackage(
            id = "puz_${System.currentTimeMillis()}",
            title = title.ifBlank { "Teka-Teki Ceria TK" },
            description = description.ifBlank { "Logika hewan seru" },
            author = author.ifBlank { "Guru TK" },
            questions = questions,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.savePuzzle(newPuzzle)
            _bannerMessage.value = "Teka-Teki baru '${newPuzzle.title}' berhasil disimpan!"
        }
    }

    fun deletePuzzle(id: String) {
        viewModelScope.launch {
            repository.deletePuzzle(id)
            _bannerMessage.value = "Teka-Teki berhasil dihapus."
        }
    }

    fun deleteSubmission(id: Long) {
        viewModelScope.launch {
            repository.deleteSubmission(id)
        }
    }

    fun clearAllSubmissions() {
        viewModelScope.launch {
            repository.clearAllSubmissions()
            _bannerMessage.value = "Semua riwayat nilai siswa dibersihkan."
        }
    }

    companion object {
        fun provideFactory(repository: EduRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EduViewModel(repository) as T
                }
            }
        }
    }
}
