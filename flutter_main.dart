/**
 * APLIKASI EDUKASI TK OFFLINE-FIRST: TEKA-TEKI LOGIKA HEWAN & QR CODE
 * 
 * Arsitektur: Clean Architecture / State Management dengan ChangeNotifier
 * Dependensi utama pubspec.yaml:
 *   flutter:
 *     sdk: flutter
 *   qr_flutter: ^4.1.0       # Untuk generate QR Code offline
 *   mobile_scanner: ^5.2.3   # Untuk scan QR Code menggunakan kamera
 *   google_fonts: ^6.2.1     # Opsional untuk font ramah anak
 */

import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:qr_flutter/qr_flutter.dart';
import 'package:mobile_scanner/mobile_scanner.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const EduTKApp());
}

// ==========================================
// 1. MODEL DATA (DOMAIN & ENTITY)
// ==========================================

/// Model satu pertanyaan teka-teki logika hewan untuk anak TK
class AnimalQuestion {
  final String id;
  final String question;
  final String animalEmoji;
  final List<String> options;
  final int correctIndex;
  final String explanation;
  final String soundText;

  AnimalQuestion({
    required this.id,
    required this.question,
    required this.animalEmoji,
    required this.options,
    required this.correctIndex,
    this.explanation = '',
    this.soundText = '',
  });

  Map<String, dynamic> toJson() => {
    'id': id,
    'question': question,
    'emoji': animalEmoji,
    'options': options,
    'ans': correctIndex,
    'expl': explanation,
    'sound': soundText,
  };

  factory AnimalQuestion.fromJson(Map<String, dynamic> json) => AnimalQuestion(
    id: json['id'] ?? 'q_${DateTime.now().millisecondsSinceEpoch}',
    question: json['question'] ?? '',
    animalEmoji: json['emoji'] ?? '🐾',
    options: List<String>.from(json['options'] ?? []),
    correctIndex: json['ans'] ?? 0,
    explanation: json['expl'] ?? '',
    soundText: json['sound'] ?? '',
  );
}

/// Model paket teka-teki yang dibuat guru untuk dibagikan lewat QR Code
class PuzzlePackage {
  final String id;
  final String title;
  final String description;
  final String author;
  final List<AnimalQuestion> questions;
  final int createdAt;

  PuzzlePackage({
    required this.id,
    required this.title,
    required this.description,
    required this.author,
    required this.questions,
    int? createdAt,
  }) : createdAt = createdAt ?? DateTime.now().millisecondsSinceEpoch;

  Map<String, dynamic> toJson() => {
    'type': 'EDUTK_PUZZLE',
    'id': id,
    'title': title,
    'desc': description,
    'author': author,
    'questions': questions.map((q) => q.toJson()).toList(),
    'ts': createdAt,
  };

  factory PuzzlePackage.fromJson(Map<String, dynamic> json) => PuzzlePackage(
    id: json['id'] ?? 'puz_${DateTime.now().millisecondsSinceEpoch}',
    title: json['title'] ?? 'Teka-Teki TK',
    description: json['desc'] ?? 'Logika hewan seru',
    author: json['author'] ?? 'Guru TK',
    questions: (json['questions'] as List? ?? [])
        .map((item) => AnimalQuestion.fromJson(Map<String, dynamic>.from(item)))
        .toList(),
    createdAt: json['ts'] ?? DateTime.now().millisecondsSinceEpoch,
  );
}

/// Model data hasil belajar siswa yang di-generate jadi QR Code
class StudentSubmission {
  final String studentName;
  final String puzzleTitle;
  final int score;
  final int totalQuestions;
  final int stars;
  final int timestamp;
  final String notes;

  StudentSubmission({
    required this.studentName,
    required this.puzzleTitle,
    required this.score,
    required this.totalQuestions,
    required this.stars,
    int? timestamp,
    this.notes = '',
  }) : timestamp = timestamp ?? DateTime.now().millisecondsSinceEpoch;

  int get percentage => totalQuestions > 0 ? ((score / totalQuestions) * 100).toInt() : 0;

  Map<String, dynamic> toJson() => {
    'type': 'EDUTK_RESULT',
    'student': studentName,
    'puzzle': puzzleTitle,
    'score': score,
    'total': totalQuestions,
    'stars': stars,
    'ts': timestamp,
    'notes': notes,
  };

  factory StudentSubmission.fromJson(Map<String, dynamic> json) => StudentSubmission(
    studentName: json['student'] ?? 'Siswa TK',
    puzzleTitle: json['puzzle'] ?? 'Teka-Teki Hewan',
    score: json['score'] ?? 0,
    totalQuestions: json['total'] ?? 0,
    stars: json['stars'] ?? 3,
    timestamp: json['ts'] ?? DateTime.now().millisecondsSinceEpoch,
    notes: json['notes'] ?? '',
  );
}

// ==========================================
// 2. STATE MANAGEMENT (OFFLINE CONTROLLER)
// ==========================================

enum UserRole { student, teacher }

class EduTKController extends ChangeNotifier {
  UserRole _role = UserRole.student;
  UserRole get role => _role;

  String _studentName = 'Aisyah';
  String get studentName => _studentName;

  // Bank Teka-Teki bawaan & buatan guru (Offline Storage)
  final List<PuzzlePackage> _puzzles = [];
  List<PuzzlePackage> get puzzles => List.unmodifiable(_puzzles);

  // Daftar Nilai Siswa yang berhasil di-scan oleh Guru
  final List<StudentSubmission> _submissions = [];
  List<StudentSubmission> get submissions => List.unmodifiable(_submissions);

  // State Quiz Siswa yang sedang berjalan
  PuzzlePackage? _activePuzzle;
  PuzzlePackage? get activePuzzle => _activePuzzle;

  int _currentQuestionIndex = 0;
  int get currentQuestionIndex => _currentQuestionIndex;

  int? _selectedOption;
  int? get selectedOption => _selectedOption;

  bool _isAnswerSubmitted = false;
  bool get isAnswerSubmitted => _isAnswerSubmitted;

  int _score = 0;
  int get score => _score;

  bool _isQuizFinished = false;
  bool get isQuizFinished => _isQuizFinished;

  EduTKController() {
    _seedDefaultPuzzles();
  }

  void switchRole(UserRole newRole) {
    _role = newRole;
    notifyListeners();
  }

  void setStudentName(String name) {
    if (name.trim().isNotEmpty) {
      _studentName = name.trim();
      notifyListeners();
    }
  }

  // Permainan Siswa
  void startPuzzle(PuzzlePackage puzzle) {
    _activePuzzle = puzzle;
    _currentQuestionIndex = 0;
    _selectedOption = null;
    _isAnswerSubmitted = false;
    _score = 0;
    _isQuizFinished = false;
    notifyListeners();
  }

  void resetQuiz() {
    _activePuzzle = null;
    _isQuizFinished = false;
    _selectedOption = null;
    _isAnswerSubmitted = false;
    notifyListeners();
  }

  void selectOption(int index) {
    if (!_isAnswerSubmitted) {
      _selectedOption = index;
      notifyListeners();
    }
  }

  void submitAnswer() {
    if (_activePuzzle == null || _selectedOption == null) return;
    final q = _activePuzzle!.questions[_currentQuestionIndex];
    _isAnswerSubmitted = true;
    if (_selectedOption == q.correctIndex) {
      _score++;
    }
    notifyListeners();
  }

  void nextQuestion() {
    if (_activePuzzle == null) return;
    if (_currentQuestionIndex + 1 < _activePuzzle!.questions.length) {
      _currentQuestionIndex++;
      _selectedOption = null;
      _isAnswerSubmitted = false;
    } else {
      _isQuizFinished = true;
    }
    notifyListeners();
  }

  // Tambah Soal Guru
  void addPuzzle(PuzzlePackage puzzle) {
    _puzzles.insert(0, puzzle);
    notifyListeners();
  }

  void deletePuzzle(String id) {
    _puzzles.removeWhere((p) => p.id == id);
    notifyListeners();
  }

  // Simpan Hasil Nilai Siswa (Hasil Scan QR Siswa oleh Guru)
  void recordSubmission(StudentSubmission sub) {
    _submissions.insert(0, sub);
    notifyListeners();
  }

  void deleteSubmission(int index) {
    _submissions.removeAt(index);
    notifyListeners();
  }

  void clearSubmissions() {
    _submissions.clear();
    notifyListeners();
  }

  // Pre-seed bank soal awal logika anak TK
  void _seedDefaultPuzzles() {
    _puzzles.addAll([
      PuzzlePackage(
        id: 'puz_hewan_darat',
        title: 'Sahabat Hewan Darat 🐮',
        description: 'Teka-teki makanan & suara hewan ramah anak',
        author: 'Bu Guru Maya',
        questions: [
          AnimalQuestion(
            id: 'q1',
            question: 'Sapi lucu 🐮 suka makan apa di padang rumput?',
            animalEmoji: '🐮',
            options: ['Rumput Hijau 🌿', 'Ikan Goreng 🐟', 'Permen 🍬'],
            correctIndex: 0,
            explanation: 'Sapi suka makan rumput hijau segar!',
            soundText: 'Moo~ Moo!',
          ),
          AnimalQuestion(
            id: 'q2',
            question: 'Hewan mana yang bersuara "Guk guk!" dengan ceria?',
            animalEmoji: '🐶',
            options: ['Kucing Manis 🐱', 'Anjing Sahabat 🐶', 'Ayam Jantan 🐓'],
            correctIndex: 1,
            explanation: 'Anjing menyapa kita dengan suara guk guk!',
            soundText: 'Guk! Guk!',
          ),
          AnimalQuestion(
            id: 'q3',
            question: 'Hewan berbadan besar dan punya belalai panjang adalah...',
            animalEmoji: '🐘',
            options: ['Gajah Besar 🐘', 'Kelinci Mungil 🐰', 'Monyet 🐵'],
            correctIndex: 0,
            explanation: 'Gajah memiliki belalai panjang untuk minum!',
            soundText: 'Trruuumph!',
          ),
          AnimalQuestion(
            id: 'q4',
            question: 'Siapa yang melompat lincah dan suka makan wortel? 🥕',
            animalEmoji: '🐰',
            options: ['Kelinci Lucu 🐰', 'Singa Hutan 🦁', 'Bebek 🦆'],
            correctIndex: 0,
            explanation: 'Kelinci bertelinga panjang dan suka wortel!',
            soundText: 'Hop! Hop!',
          ),
        ],
      ),
      PuzzlePackage(
        id: 'puz_hewan_air_langit',
        title: 'Hewan Air & Angkasa 🐬',
        description: 'Mengenal hewan laut dan burung yang bisa terbang',
        author: 'Bu Guru Maya',
        questions: [
          AnimalQuestion(
            id: 'qa1',
            question: 'Hewan cerdas yang bisa berenang cepat di laut adalah...',
            animalEmoji: '🐬',
            options: ['Lumba-lumba 🐬', 'Kambing 🐐', 'Kucing 🐱'],
            correctIndex: 0,
            explanation: 'Lumba-lumba berenang dan melompat di air laut!',
            soundText: 'Klik-klik!',
          ),
          AnimalQuestion(
            id: 'qa2',
            question: 'Burung yang bangun di malam hari dan matanya bulat adalah...',
            animalEmoji: '🦉',
            options: ['Burung Hantu 🦉', 'Ayam Jago 🐓', 'Bebek 🦆'],
            correctIndex: 0,
            explanation: 'Burung hantu melihat jelas di malam hari!',
            soundText: 'Huuu-huuu!',
          ),
        ],
      ),
    ]);

    _submissions.addAll([
      StudentSubmission(
        studentName: 'Fathir (TK-B)',
        puzzleTitle: 'Sahabat Hewan Darat 🐮',
        score: 4,
        totalQuestions: 4,
        stars: 3,
        notes: 'Sempurna! Semua benar.',
      ),
      StudentSubmission(
        studentName: 'Nadia (TK-A)',
        puzzleTitle: 'Hewan Air & Angkasa 🐬',
        score: 2,
        totalQuestions: 2,
        stars: 3,
        notes: 'Sangat mandiri dan ceria.',
      ),
    ]);
  }
}

// ==========================================
// 3. WIDGET ROOT APLIKASI
// ==========================================

class EduTKApp extends StatefulWidget {
  const EduTKApp({super.key});

  @override
  State<EduTKApp> createState() => _EduTKAppState();
}

class _EduTKAppState extends State<EduTKApp> {
  final EduTKController _controller = EduTKController();

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _controller,
      builder: (context, _) {
        return MaterialApp(
          title: 'EduTK Ceria',
          debugShowCheckedModeBanner: false,
          theme: ThemeData(
            useMaterial3: true,
            colorScheme: ColorScheme.fromSeed(
              seedColor: const Color(0xFFFF9800), // Sunny Orange
              primary: const Color(0xFFF57C00),
              secondary: const Color(0xFF4CAF50), // Grass Green
              surface: Colors.white,
            ),
            scaffoldBackgroundColor: const Color(0xFFFFFBF0), // Warm background
          ),
          home: HomeScreen(controller: _controller),
        );
      },
    );
  }
}

// ==========================================
// 4. SCREEN UTAMA & NAVIGASI ROLE
// ==========================================

class HomeScreen extends StatelessWidget {
  final EduTKController controller;

  const HomeScreen({super.key, required this.controller});

  @override
  Widget build(BuildContext context) {
    final isStudent = controller.role == UserRole.student;

    return Scaffold(
      appBar: AppBar(
        backgroundColor: const Color(0xFFFFE082),
        elevation: 0,
        title: Row(
          children: [
            Text(isStudent ? '🦁 EduTK Ceria' : '👩‍🏫 Dasbor Guru TK',
                style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 18)),
          ],
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 12.0),
            child: ElevatedButton.icon(
              onPressed: () {
                controller.switchRole(
                  isStudent ? UserRole.teacher : UserRole.student,
                );
              },
              icon: Icon(isStudent ? Icons.school : Icons.smart_toy, size: 16),
              label: Text(
                isStudent ? 'Mode Guru' : 'Mode Siswa',
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12),
              ),
              style: ElevatedButton.styleFrom(
                backgroundColor: isStudent ? const Color(0xFF0288D1) : const Color(0xFFFF9800),
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              ),
            ),
          ),
        ],
      ),
      body: isStudent
          ? StudentDashboard(controller: controller)
          : TeacherDashboard(controller: controller),
    );
  }
}

// ==========================================
// 5. DASBOR SISWA (STUDENT DASHBOARD)
// ==========================================

class StudentDashboard extends StatelessWidget {
  final EduTKController controller;

  const StudentDashboard({super.key, required this.controller});

  @override
  Widget build(BuildContext context) {
    if (controller.activePuzzle == null) {
      return _buildStudentHome(context);
    } else if (controller.isQuizFinished) {
      return _buildQuizResult(context);
    } else {
      return _buildActiveQuiz(context);
    }
  }

  // Tampilan Beranda Siswa
  Widget _buildStudentHome(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16.0),
      children: [
        // Banner Sambutan Ceria
        Container(
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            gradient: const LinearGradient(
              colors: [Color(0xFFFFCA28), Color(0xFFFF9800)],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
            borderRadius: BorderRadius.circular(24),
            boxShadow: [
              BoxShadow(
                color: Colors.orange.withOpacity(0.3),
                blurRadius: 10,
                offset: const Offset(0, 4),
              ),
            ],
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('🎒 Halo, Sahabat Cilik!',
                      style: TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.bold)),
                  IconButton(
                    icon: const Icon(Icons.edit, color: Colors.white, size: 20),
                    onPressed: () => _showEditNameDialog(context),
                  ),
                ],
              ),
              const SizedBox(height: 6),
              Text(
                'Pemain: ${controller.studentName} ⭐',
                style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.w600),
              ),
              const SizedBox(height: 14),
              ElevatedButton.icon(
                onPressed: () => _openScanner(context, ScanTarget.teacherPuzzle),
                icon: const Icon(Icons.qr_code_scanner, color: Color(0xFFE65100)),
                label: const Text('Scan QR Soal Guru 📷',
                    style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFFE65100))),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 24),

        const Text('Pilih Teka-Teki Logika Hewan: 🦁',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
        const SizedBox(height: 12),

        ...controller.puzzles.map((puzzle) {
          return Card(
            margin: const EdgeInsets.only(bottom: 12),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
            elevation: 3,
            color: const Color(0xFFFFF8E1),
            child: InkWell(
              borderRadius: BorderRadius.circular(20),
              onTap: () => controller.startPuzzle(puzzle),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Row(
                  children: [
                    Container(
                      width: 54,
                      height: 54,
                      decoration: const BoxDecoration(
                        color: Colors.white,
                        shape: BoxShape.circle,
                      ),
                      alignment: Alignment.center,
                      child: Text(
                        puzzle.questions.isNotEmpty ? puzzle.questions.first.animalEmoji : '🐾',
                        style: const TextStyle(fontSize: 32),
                      ),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(puzzle.title,
                              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                          const SizedBox(height: 4),
                          Text('${puzzle.questions.length} Soal • ${puzzle.description}',
                              style: TextStyle(fontSize: 12, color: Colors.brown.shade600)),
                        ],
                      ),
                    ),
                    const Icon(Icons.play_circle_fill, color: Color(0xFFFF9800), size: 40),
                  ],
                ),
              ),
            ),
          );
        }).toList(),
      ],
    );
  }

  // Tampilan Game Kuis Pilihan Teka-Teki Hewan
  Widget _buildActiveQuiz(BuildContext context) {
    final puzzle = controller.activePuzzle!;
    final q = puzzle.questions[controller.currentQuestionIndex];
    final progress = (controller.currentQuestionIndex + 1) / puzzle.questions.length;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        children: [
          // Header Progress & Nilai Bintang
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () => controller.resetQuiz(),
              ),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 12),
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(8),
                    child: LinearProgressIndicator(
                      value: progress,
                      minHeight: 10,
                      backgroundColor: Colors.grey.shade300,
                      valueColor: const AlwaysStoppedAnimation(Color(0xFF4CAF50)),
                    ),
                  ),
                ),
              ),
              Row(
                children: [
                  const Icon(Icons.star, color: Colors.amber, size: 22),
                  const SizedBox(width: 4),
                  Text('${controller.score}',
                      style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                ],
              ),
            ],
          ),
          const SizedBox(height: 16),

          // Kartu Soal Hewan dengan Emoji Besar
          Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
            elevation: 4,
            child: Padding(
              padding: const EdgeInsets.all(20.0),
              child: Column(
                children: [
                  Container(
                    width: 90,
                    height: 90,
                    decoration: BoxDecoration(
                      color: const Color(0xFFFFF3E0),
                      shape: BoxShape.circle,
                      border: Border.all(color: Colors.orange.shade200, width: 3),
                    ),
                    alignment: Alignment.center,
                    child: Text(q.animalEmoji, style: const TextStyle(fontSize: 50)),
                  ),
                  if (q.soundText.isNotEmpty) ...[
                    const SizedBox(height: 8),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                      decoration: BoxDecoration(
                        color: Colors.amber.shade100,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text('"${q.soundText}"',
                          style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.brown)),
                    ),
                  ],
                  const SizedBox(height: 14),
                  Text(
                    q.question,
                    textAlign: TextAlign.center,
                    style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 20),

          // Tombol Pilihan Jawaban Ramah Anak (Besar & Berwarna)
          ...List.generate(q.options.length, (index) {
            final isSelected = controller.selectedOption == index;
            final isCorrect = index == q.correctIndex;
            Color btnColor = Colors.white;
            Color textColor = Colors.black87;

            if (controller.isAnswerSubmitted) {
              if (isCorrect) {
                btnColor = const Color(0xFF4CAF50);
                textColor = Colors.white;
              } else if (isSelected) {
                btnColor = const Color(0xFFFF5252);
                textColor = Colors.white;
              }
            } else if (isSelected) {
              btnColor = const Color(0xFFFF9800);
              textColor = Colors.white;
            }

            return Padding(
              padding: const EdgeInsets.only(bottom: 12.0),
              child: SizedBox(
                width: double.infinity,
                height: 60,
                child: ElevatedButton(
                  onPressed: controller.isAnswerSubmitted
                      ? null
                      : () => controller.selectOption(index),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: btnColor,
                    foregroundColor: textColor,
                    elevation: isSelected ? 4 : 1,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(18),
                      side: BorderSide(
                        color: isSelected ? Colors.orange : Colors.grey.shade300,
                        width: 2,
                      ),
                    ),
                  ),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(q.options[index],
                          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: textColor)),
                      if (controller.isAnswerSubmitted)
                        Icon(isCorrect ? Icons.check_circle : Icons.cancel, color: Colors.white),
                    ],
                  ),
                ),
              ),
            );
          }),

          const SizedBox(height: 14),

          // Tombol Aksi (Kunci Jawaban atau Soal Berikutnya)
          if (!controller.isAnswerSubmitted)
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: controller.selectedOption == null
                    ? null
                    : () => controller.submitAnswer(),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFFFF9800),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
                ),
                child: const Text('Kunci Jawaban! 🎯',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              ),
            )
          else
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: () => controller.nextQuestion(),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF4CAF50),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
                ),
                child: Text(
                  controller.currentQuestionIndex + 1 < puzzle.questions.length
                      ? 'Soal Berikutnya 👉'
                      : 'Lihat Hasil Kuis! 🏆',
                  style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
              ),
            ),
        ],
      ),
    );
  }

  // Tampilan Hasil Kuis & Tombol Generate QR Hasil Nilai
  Widget _buildQuizResult(BuildContext context) {
    final puzzle = controller.activePuzzle!;
    final total = puzzle.questions.length;
    final score = controller.score;
    final stars = score == total ? 3 : (score >= total / 2 ? 2 : 1);

    final submission = StudentSubmission(
      studentName: controller.studentName,
      puzzleTitle: puzzle.title,
      score: score,
      totalQuestions: total,
      stars: stars,
    );

    return SingleChildScrollView(
      padding: const EdgeInsets.all(24.0),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text('🏆', style: TextStyle(fontSize: 70)),
          const SizedBox(height: 10),
          Text('Hebat, ${controller.studentName}! 🎉',
              style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold)),
          const SizedBox(height: 4),
          Text('Telah menyelesaikan "${puzzle.title}"',
              style: TextStyle(color: Colors.grey.shade700)),
          const SizedBox(height: 16),

          // Bintang Nilai
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: List.generate(3, (i) {
              return Icon(
                Icons.star,
                size: 48,
                color: i < stars ? Colors.amber : Colors.grey.shade300,
              );
            }),
          ),
          const SizedBox(height: 16),

          // Kartu Skor
          Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
            child: Padding(
              padding: const EdgeInsets.all(20.0),
              child: Column(
                children: [
                  const Text('Skor Pengerjaan:'),
                  Text('$score / $total',
                      style: const TextStyle(
                          fontSize: 40, fontWeight: FontWeight.w900, color: Color(0xFF4CAF50))),
                  Text('${submission.percentage}% Benar',
                      style: const TextStyle(fontWeight: FontWeight.bold)),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),

          // FITUR GENERATE QR CODE HASIL PENGERJAAN
          SizedBox(
            width: double.infinity,
            height: 56,
            child: ElevatedButton.icon(
              onPressed: () {
                _showQrDialog(
                  context,
                  title: 'QR Nilai ${controller.studentName}',
                  subtitle: 'Tunjukkan QR Code ini ke Bu Guru untuk dicatat nilainya!',
                  data: jsonEncode(submission.toJson()),
                );
              },
              icon: const Icon(Icons.qr_code, size: 24),
              label: const Text('Buat QR Nilai untuk Bu Guru 📱',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFFF9800),
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
              ),
            ),
          ),
          const SizedBox(height: 12),

          OutlinedButton(
            onPressed: () => controller.resetQuiz(),
            style: OutlinedButton.styleFrom(
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              minimumSize: const Size(double.infinity, 48),
            ),
            child: const Text('Kembali ke Beranda'),
          ),
        ],
      ),
    );
  }

  void _showEditNameDialog(BuildContext context) {
    final textController = TextEditingController(text: controller.studentName);
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Ganti Nama Siswa'),
        content: TextField(
          controller: textController,
          decoration: const InputDecoration(hintText: 'Contoh: Aisyah / Budi'),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Batal')),
          ElevatedButton(
            onPressed: () {
              controller.setStudentName(textController.text);
              Navigator.pop(ctx);
            },
            child: const Text('Simpan'),
          ),
        ],
      ),
    );
  }
}

// ==========================================
// 6. DASBOR GURU (TEACHER DASHBOARD)
// ==========================================

class TeacherDashboard extends StatefulWidget {
  final EduTKController controller;

  const TeacherDashboard({super.key, required this.controller});

  @override
  State<TeacherDashboard> createState() => _TeacherDashboardState();
}

class _TeacherDashboardState extends State<TeacherDashboard>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        // Tab Bar Guru: Daftar Siswa, Bank Soal, Buat Teka-Teki
        Container(
          color: Colors.white,
          child: TabBar(
            controller: _tabController,
            indicatorColor: const Color(0xFFFF9800),
            labelColor: const Color(0xFFF57C00),
            unselectedLabelColor: Colors.grey,
            tabs: const [
              Tab(icon: Icon(Icons.people), text: 'Daftar Siswa'),
              Tab(icon: Icon(Icons.menu_book), text: 'Bank Soal & QR'),
              Tab(icon: Icon(Icons.add_circle), text: 'Buat Soal'),
            ],
          ),
        ),
        Expanded(
          child: TabBarView(
            controller: _tabController,
            children: [
              _buildStudentSubmissionsTab(context),
              _buildPuzzleBankTab(context),
              _buildCreatePuzzleTab(context),
            ],
          ),
        ),
      ],
    );
  }

  // Tab 1: Daftar Siswa & Scan QR Hasil Nilai
  Widget _buildStudentSubmissionsTab(BuildContext context) {
    final list = widget.controller.submissions;

    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Daftar Nilai Siswa',
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                  Text('${list.length} Nilai Tersimpan Offline',
                      style: TextStyle(fontSize: 12, color: Colors.grey.shade600)),
                ],
              ),
              ElevatedButton.icon(
                onPressed: () => _openScanner(context, ScanTarget.studentResult),
                icon: const Icon(Icons.qr_code_scanner, size: 18),
                label: const Text('Scan QR Siswa 📷'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF4CAF50),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          if (list.isEmpty)
            Expanded(
              child: Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(Icons.assignment_outlined, size: 60, color: Colors.grey),
                    const SizedBox(height: 10),
                    const Text('Belum ada nilai siswa yang di-scan.',
                        style: TextStyle(color: Colors.grey)),
                    const SizedBox(height: 12),
                    ElevatedButton(
                      onPressed: () => _openScanner(context, ScanTarget.studentResult),
                      child: const Text('Scan QR Nilai Siswa Sekarang'),
                    ),
                  ],
                ),
              ),
            )
          else
            Expanded(
              child: ListView.builder(
                itemCount: list.length,
                itemBuilder: (ctx, i) {
                  final item = list[i];
                  return Card(
                    margin: const EdgeInsets.only(bottom: 8),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                    child: ListTile(
                      leading: CircleAvatar(
                        backgroundColor: Colors.orange.shade100,
                        child: const Text('🧒'),
                      ),
                      title: Text(item.studentName,
                          style: const TextStyle(fontWeight: FontWeight.bold)),
                      subtitle: Text('${item.puzzleTitle} • ${item.notes}'),
                      trailing: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            crossAxisAlignment: CrossAxisAlignment.end,
                            children: [
                              Text('${item.score}/${item.totalQuestions}',
                                  style: const TextStyle(
                                      fontWeight: FontWeight.bold,
                                      fontSize: 16,
                                      color: Colors.green)),
                              Row(
                                children: List.generate(
                                  item.stars,
                                  (_) => const Icon(Icons.star, size: 12, color: Colors.amber),
                                ),
                              ),
                            ],
                          ),
                          IconButton(
                            icon: const Icon(Icons.delete_outline, color: Colors.grey),
                            onPressed: () => widget.controller.deleteSubmission(i),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
        ],
      ),
    );
  }

  // Tab 2: Bank Soal & Generate QR Soal untuk Siswa
  Widget _buildPuzzleBankTab(BuildContext context) {
    final puzzles = widget.controller.puzzles;

    return ListView.builder(
      padding: const EdgeInsets.all(16.0),
      itemCount: puzzles.length,
      itemBuilder: (ctx, i) {
        final p = puzzles[i];
        return Card(
          margin: const EdgeInsets.only(bottom: 12),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(p.title,
                        style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                    if (!p.id.startsWith('puz_hewan_'))
                      IconButton(
                        icon: const Icon(Icons.delete_outline, color: Colors.grey),
                        onPressed: () => widget.controller.deletePuzzle(p.id),
                      ),
                  ],
                ),
                Text('${p.questions.length} Soal • ${p.description}',
                    style: TextStyle(color: Colors.grey.shade700, fontSize: 13)),
                const SizedBox(height: 12),

                // FITUR GENERATE QR CODE SOAL OLEH GURU
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    onPressed: () {
                      _showQrDialog(
                        context,
                        title: 'QR Soal: ${p.title}',
                        subtitle: 'Siswa dapat scan QR ini untuk membuka soal secara offline.',
                        data: jsonEncode(p.toJson()),
                      );
                    },
                    icon: const Icon(Icons.qr_code, size: 20),
                    label: const Text('Tampilkan QR Code Soal 📱',
                        style: TextStyle(fontWeight: FontWeight.bold)),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFFFF9800),
                      foregroundColor: Colors.white,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                    ),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  // Tab 3: Form Buat Teka-Teki Baru
  Widget _buildCreatePuzzleTab(BuildContext context) {
    final titleCtrl = TextEditingController();
    final descCtrl = TextEditingController();

    // Pertanyaan yang sedang dirancang
    final qTitleCtrl = TextEditingController();
    final opt1Ctrl = TextEditingController();
    final opt2Ctrl = TextEditingController();
    final opt3Ctrl = TextEditingController();
    int correctAns = 0;
    String selectedEmoji = '🦁';

    final questions = <AnimalQuestion>[];

    return StatefulBuilder(
      builder: (context, setInnerState) {
        return SingleChildScrollView(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('Buat Teka-Teki Logika Hewan Baru ✍️',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              const SizedBox(height: 12),

              TextField(
                controller: titleCtrl,
                decoration: InputDecoration(
                  labelText: 'Judul Teka-Teki',
                  hintText: 'Contoh: Hewan Rimba Ceria 🦁',
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                ),
              ),
              const SizedBox(height: 10),

              TextField(
                controller: descCtrl,
                decoration: InputDecoration(
                  labelText: 'Deskripsi Singkat',
                  hintText: 'Contoh: Teka-teki logika untuk anak TK-B',
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                ),
              ),
              const SizedBox(height: 20),

              Text('Daftar Soal (${questions.length} ditambahkan):',
                  style: const TextStyle(fontWeight: FontWeight.bold)),
              ...questions.map((q) => ListTile(
                    dense: true,
                    leading: Text(q.animalEmoji, style: const TextStyle(fontSize: 20)),
                    title: Text(q.question),
                    subtitle: Text('Kunci: ${q.options[q.correctIndex]}'),
                  )),
              const SizedBox(height: 12),

              // Box Tambah Soal Satuan
              Card(
                color: Colors.orange.shade50,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                child: Padding(
                  padding: const EdgeInsets.all(14.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text('Tambah Pertanyaan Baru:',
                          style: TextStyle(fontWeight: FontWeight.bold, color: Colors.brown)),
                      const SizedBox(height: 8),

                      // Emoji Selector
                      Wrap(
                        spacing: 8,
                        children: ['🦁', '🐘', '🐮', '🐶', '🐱', '🐰', '🐬', '🦉'].map((em) {
                          final isSel = selectedEmoji == em;
                          return ChoiceChip(
                            label: Text(em, style: const TextStyle(fontSize: 18)),
                            selected: isSel,
                            onSelected: (_) => setInnerState(() => selectedEmoji = em),
                          );
                        }).toList(),
                      ),
                      const SizedBox(height: 10),

                      TextField(
                        controller: qTitleCtrl,
                        decoration: InputDecoration(
                          labelText: 'Teks Soal Teka-Teki',
                          hintText: 'Contoh: Hewan apa yang lehernya panjang? 🦒',
                          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                        ),
                      ),
                      const SizedBox(height: 8),

                      TextField(
                        controller: opt1Ctrl,
                        decoration: InputDecoration(
                          labelText: 'Pilihan 1 (Kunci)',
                          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                        ),
                      ),
                      const SizedBox(height: 6),

                      TextField(
                        controller: opt2Ctrl,
                        decoration: InputDecoration(
                          labelText: 'Pilihan 2',
                          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                        ),
                      ),
                      const SizedBox(height: 6),

                      TextField(
                        controller: opt3Ctrl,
                        decoration: InputDecoration(
                          labelText: 'Pilihan 3',
                          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                        ),
                      ),
                      const SizedBox(height: 10),

                      ElevatedButton.icon(
                        onPressed: () {
                          if (qTitleCtrl.text.isNotEmpty &&
                              opt1Ctrl.text.isNotEmpty &&
                              opt2Ctrl.text.isNotEmpty) {
                            setInnerState(() {
                              questions.add(AnimalQuestion(
                                id: 'q_${DateTime.now().millisecondsSinceEpoch}',
                                question: qTitleCtrl.text.trim(),
                                animalEmoji: selectedEmoji,
                                options: [
                                  opt1Ctrl.text.trim(),
                                  opt2Ctrl.text.trim(),
                                  opt3Ctrl.text.trim().isNotEmpty
                                      ? opt3Ctrl.text.trim()
                                      : 'Lainnya',
                                ],
                                correctIndex: 0,
                                explanation: 'Pilihan yang sangat tepat!',
                              ));
                              qTitleCtrl.clear();
                              opt1Ctrl.clear();
                              opt2Ctrl.clear();
                              opt3Ctrl.clear();
                            });
                          }
                        },
                        icon: const Icon(Icons.add),
                        label: const Text('Masukkan ke Daftar Soal'),
                      ),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 20),
              SizedBox(
                width: double.infinity,
                height: 52,
                child: ElevatedButton(
                  onPressed: () {
                    if (titleCtrl.text.isNotEmpty && questions.isNotEmpty) {
                      widget.controller.addPuzzle(PuzzlePackage(
                        id: 'puz_${DateTime.now().millisecondsSinceEpoch}',
                        title: titleCtrl.text.trim(),
                        description: descCtrl.text.trim().isNotEmpty
                            ? descCtrl.text.trim()
                            : 'Teka-teki logika hewan',
                        author: 'Guru TK',
                        questions: List.from(questions),
                      ));
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Teka-Teki Baru Berhasil Disimpan!')),
                      );
                      _tabController.animateTo(1); // Pindah ke Tab Bank Soal
                    }
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFFFF9800),
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  ),
                  child: const Text('Simpan Paket Teka-Teki & Siapkan QR 🌟',
                      style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

// ==========================================
// 7. DIALOG GENERATE QR CODE (OFFLINE)
// ==========================================

void _showQrDialog(BuildContext context,
    {required String title, required String subtitle, required String data}) {
  showDialog(
    context: context,
    builder: (ctx) => AlertDialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      title: Text(title, textAlign: TextAlign.center, style: const TextStyle(fontWeight: FontWeight.bold)),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(subtitle, textAlign: TextAlign.center, style: TextStyle(fontSize: 13, color: Colors.grey.shade600)),
          const SizedBox(height: 16),
          // Widget QR Code menggunakan qr_flutter
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: Colors.orange.shade300, width: 3),
            ),
            child: QrImageView(
              data: data,
              version: QrVersions.auto,
              size: 220.0,
              backgroundColor: Colors.white,
            ),
          ),
          const SizedBox(height: 12),
          const Text('✅ 100% Offline (Tanpa Internet)',
              style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Colors.green)),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(ctx),
          child: const Text('Tutup', style: TextStyle(fontWeight: FontWeight.bold)),
        ),
      ],
    ),
  );
}

// ==========================================
// 8. SCREEN PEMINDAI QR CODE (MOBILE SCANNER)
// ==========================================

enum ScanTarget { teacherPuzzle, studentResult }

void _openScanner(BuildContext context, ScanTarget target) {
  Navigator.push(
    context,
    MaterialPageRoute(
      builder: (ctx) => QrScannerScreen(target: target),
    ),
  );
}

class QrScannerScreen extends StatefulWidget {
  final ScanTarget target;

  const QrScannerScreen({super.key, required this.target});

  @override
  State<QrScannerScreen> createState() => _QrScannerScreenState();
}

class _QrScannerScreenState extends State<QrScannerScreen> {
  final MobileScannerController _scannerController = MobileScannerController();
  bool _hasScanned = false;

  @override
  void dispose() {
    _scannerController.dispose();
    super.dispose();
  }

  void _handleBarcode(BarcodeCapture capture) {
    if (_hasScanned) return;
    final List<Barcode> barcodes = capture.barcodes;
    for (final barcode in barcodes) {
      final String? code = barcode.rawValue;
      if (code != null && code.isNotEmpty) {
        _hasScanned = true;
        _processScannedCode(code);
        break;
      }
    }
  }

  void _processScannedCode(String raw) {
    try {
      final data = jsonDecode(raw);
      if (widget.target == ScanTarget.studentResult && data['type'] == 'EDUTK_RESULT') {
        final submission = StudentSubmission.fromJson(data);
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: Colors.green,
            content: Text('Nilai ${submission.studentName} (${submission.score}/${submission.totalQuestions}) berhasil dicatat!'),
          ),
        );
      } else if (widget.target == ScanTarget.teacherPuzzle && data['type'] == 'EDUTK_PUZZLE') {
        final puzzle = PuzzlePackage.fromJson(data);
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: Colors.orange,
            content: Text('Teka-Teki "${puzzle.title}" berhasil dimuat offline!'),
          ),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Format QR Code tidak sesuai.')),
        );
        _hasScanned = false;
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Gagal membaca data QR.')),
      );
      _hasScanned = false;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.target == ScanTarget.studentResult
            ? 'Pindai QR Nilai Siswa'
            : 'Pindai QR Soal Guru'),
        actions: [
          IconButton(
            icon: const Icon(Icons.flash_on),
            onPressed: () => _scannerController.toggleTorch(),
          ),
          IconButton(
            icon: const Icon(Icons.cameraswitch),
            onPressed: () => _scannerController.switchCamera(),
          ),
        ],
      ),
      body: Stack(
        children: [
          // Kamera Pemindai MobileScanner
          MobileScanner(
            controller: _scannerController,
            onDetect: _handleBarcode,
          ),

          // Overlay Bingkai Pemindai Ceria
          Center(
            child: Container(
              width: 250,
              height: 250,
              decoration: BoxDecoration(
                border: Border.all(color: Colors.orange, width: 4),
                borderRadius: BorderRadius.circular(24),
              ),
            ),
          ),

          // Tombol Simulasi Cepat (Sangat Berguna saat pengujian di Emulator)
          Positioned(
            bottom: 30,
            left: 20,
            right: 20,
            child: Card(
              color: Colors.black.withOpacity(0.7),
              child: Padding(
                padding: const EdgeInsets.all(12.0),
                child: Column(
                  children: [
                    const Text('Uji Coba Cepat (Simulasi Offline):',
                        style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                    const SizedBox(height: 6),
                    ElevatedButton(
                      onPressed: () {
                        if (widget.target == ScanTarget.studentResult) {
                          _processScannedCode(jsonEncode(StudentSubmission(
                            studentName: 'Zaki (TK-B)',
                            puzzleTitle: 'Sahabat Hewan Darat 🐮',
                            score: 4,
                            totalQuestions: 4,
                            stars: 3,
                          ).toJson()));
                        } else {
                          _processScannedCode(jsonEncode(PuzzlePackage(
                            id: 'puz_sim',
                            title: 'Hewan Hutan Rimba 🌴',
                            description: 'Soal simulasi offline',
                            author: 'Bu Guru',
                            questions: [
                              AnimalQuestion(
                                id: 'sq1',
                                question: 'Hewan apa yang punya surai lebat dan mengaum? 🦁',
                                animalEmoji: '🦁',
                                options: ['Singa 🦁', 'Kucing 🐱', 'Bebek 🦆'],
                                correctIndex: 0,
                              ),
                            ],
                          ).toJson()));
                        }
                      },
                      child: const Text('Jalankan Simulasi Scan'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
