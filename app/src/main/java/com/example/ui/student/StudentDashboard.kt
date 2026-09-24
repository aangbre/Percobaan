package com.example.ui.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.PuzzlePackage
import com.example.ui.EduViewModel
import com.example.ui.ScanPurpose
import com.example.ui.theme.BerryPurple
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CoralPink
import com.example.ui.theme.GrassGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SunnyOrange
import com.example.ui.theme.SunnyOrangeDark
import com.example.ui.theme.SunnyYellow

@Composable
fun StudentDashboard(
    viewModel: EduViewModel,
    puzzles: List<PuzzlePackage>,
    activePuzzle: PuzzlePackage?,
    currentQuestionIndex: Int,
    selectedOption: Int?,
    isAnswerSubmitted: Boolean,
    currentScore: Int,
    isQuizFinished: Boolean,
    studentName: String,
    onOpenScanner: (ScanPurpose) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activePuzzle == null) {
        // Home selection screen for student
        StudentHomeScreen(
            puzzles = puzzles,
            studentName = studentName,
            onNameChange = { viewModel.setStudentName(it) },
            onSelectPuzzle = { viewModel.startPuzzle(it) },
            onScanTeacherQr = { onOpenScanner(ScanPurpose.STUDENT_SCAN_PUZZLE) },
            modifier = modifier
        )
    } else if (isQuizFinished) {
        // Quiz completed results screen
        StudentQuizResultScreen(
            puzzle = activePuzzle,
            studentName = studentName,
            score = currentScore,
            total = activePuzzle.questions.size,
            onGenerateResultQr = { viewModel.generateStudentResultQr() },
            onPlayAgain = { viewModel.startPuzzle(activePuzzle) },
            onBackToHome = { viewModel.resetActivePuzzle() },
            modifier = modifier
        )
    } else {
        // Active Animal Logic Puzzle game
        StudentQuizScreen(
            puzzle = activePuzzle,
            questionIndex = currentQuestionIndex,
            selectedOption = selectedOption,
            isAnswerSubmitted = isAnswerSubmitted,
            score = currentScore,
            onSelectOption = { viewModel.selectOption(it) },
            onSubmitAnswer = { viewModel.submitAnswer() },
            onNextQuestion = { viewModel.nextQuestion() },
            onExit = { viewModel.resetActivePuzzle() },
            modifier = modifier
        )
    }
}

@Composable
fun StudentHomeScreen(
    puzzles: List<PuzzlePackage>,
    studentName: String,
    onNameChange: (String) -> Unit,
    onSelectPuzzle: (PuzzlePackage) -> Unit,
    onScanTeacherQr: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember(studentName) { mutableStateOf(studentName) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("student_home_screen")
    ) {
        // Hero Image Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SunnyYellow),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.kindergarten_animals_hero),
                        contentDescription = "Hewan Ceria",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xAA000000))
                                )
                            )
                    )

                    Text(
                        text = "Hai, Sahabat Cilik! 🌟",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    )
                }

                // Greeting & Name change
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎒", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Pemain Cerdas:",
                                fontSize = 11.sp,
                                color = Color(0xFF5D4037)
                            )
                            Text(
                                text = studentName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF3E2723)
                            )
                        }
                    }

                    Button(
                        onClick = { isEditingName = !isEditingName },
                        colors = ButtonDefaults.buttonColors(containerColor = SunnyOrange),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("change_student_name_btn")
                    ) {
                        Text(
                            text = if (isEditingName) "Selesai" else "Ganti Nama",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                AnimatedVisibility(visible = isEditingName) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("Tulis Nama Siswa") },
                            placeholder = { Text("Contoh: Aisyah / Budi") },
                            modifier = Modifier.weight(1f).testTag("student_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (tempName.isNotBlank()) {
                                    onNameChange(tempName.trim())
                                }
                                isEditingName = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GrassGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Big Scan QR Button from Teacher
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onScanTeacherQr() }
                .testTag("student_scan_teacher_qr_btn"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SkyBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = SkyBlue,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Scan QR Soal Guru 📷",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Pindai QR untuk buka soal baru offline!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Text(text = "👉", fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section Title: Pilih Teka-Teki
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pilih Teka-Teki Hewan 🦁",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${puzzles.size} Pilihan",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Big kid-friendly puzzle cards
        puzzles.forEachIndexed { index, puzzle ->
            val bgColors = listOf(
                Color(0xFFFFE082), // Amber
                Color(0xFFA5D6A7), // Green
                Color(0xFFFFCCBC), // Peach/Coral
                Color(0xFFB3E5FC)  // Light Blue
            )
            val cardBg = bgColors[index % bgColors.size]

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSelectPuzzle(puzzle) }
                    .testTag("select_puzzle_${puzzle.id}_btn"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = puzzle.questions.firstOrNull()?.animalEmoji ?: "🐾",
                            fontSize = 38.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = puzzle.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2E241E)
                            )
                            Text(
                                text = "${puzzle.questions.size} Soal Logika Hewan • ${puzzle.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }

                    // Big Play Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Mulai Main",
                            tint = SunnyOrange,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun StudentQuizScreen(
    puzzle: PuzzlePackage,
    questionIndex: Int,
    selectedOption: Int?,
    isAnswerSubmitted: Boolean,
    score: Int,
    onSelectOption: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalQuestions = puzzle.questions.size
    val currentQ = puzzle.questions.getOrNull(questionIndex) ?: return
    val progress = (questionIndex + 1).toFloat() / totalQuestions

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("student_quiz_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar: Back button, Progress, Score
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onExit,
                modifier = Modifier.testTag("exit_quiz_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Keluar Teka-Teki",
                    tint = Color.DarkGray
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Soal ${questionIndex + 1} dari $totalQuestions",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = SunnyOrangeDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = GrassGreen,
                    trackColor = Color(0xFFE0E0E0),
                )
            }

            // Star Counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(SunnyYellow, RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = SunnyOrange,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$score",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723),
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Question Card with huge Animal Icon
        ElevatedCard(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Big Animal Mascot
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(SunnyYellow.copy(alpha = 0.35f), CircleShape)
                        .border(4.dp, SunnyYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = currentQ.animalEmoji, fontSize = 54.sp)
                }

                if (currentQ.soundText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF3E0)
                    ) {
                        Text(
                            text = "\"${currentQ.soundText}\"",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SunnyOrangeDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = currentQ.question,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF2E241E),
                    lineHeight = 28.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Big, colorful Option Buttons
        Text(
            text = "Pilih Jawaban yang Benar:",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        currentQ.options.forEachIndexed { optIndex, optionText ->
            val isSelected = selectedOption == optIndex
            val isCorrect = optIndex == currentQ.correctIndex

            val optionColor = when {
                isAnswerSubmitted && isCorrect -> GrassGreen
                isAnswerSubmitted && isSelected && !isCorrect -> CoralPink
                isSelected -> SunnyOrange
                else -> Color.White
            }

            val textColor = when {
                isAnswerSubmitted && (isCorrect || (isSelected && !isCorrect)) -> Color.White
                isSelected -> Color.White
                else -> Color(0xFF2E241E)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .height(64.dp)
                    .clickable(enabled = !isAnswerSubmitted) {
                        onSelectOption(optIndex)
                    }
                    .testTag("quiz_option_$optIndex"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = optionColor),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp),
                border = if (!isSelected && !isAnswerSubmitted) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD54F)) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    if (isSelected || (isAnswerSubmitted && isCorrect)) Color.White.copy(alpha = 0.3f) else SunnyYellow.copy(alpha = 0.4f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = listOf("A", "B", "C", "D").getOrElse(optIndex) { "?" },
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = optionText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    if (isAnswerSubmitted) {
                        if (isCorrect) {
                            Text(text = "✅", fontSize = 24.sp)
                        } else if (isSelected) {
                            Text(text = "❌", fontSize = 24.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback Banner after submission
        if (isAnswerSubmitted) {
            val isUserCorrect = selectedOption == currentQ.correctIndex
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUserCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isUserCorrect) "🎉 Hore! Jawabanmu Benar!" else "💡 Belum Tepat, Semangat!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUserCorrect) GrassGreen else CoralPink
                    )
                    if (currentQ.explanation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentQ.explanation,
                            fontSize = 13.sp,
                            color = Color(0xFF37474F),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Next Button
            Button(
                onClick = onNextQuestion,
                colors = ButtonDefaults.buttonColors(containerColor = GrassGreen),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("quiz_next_question_btn")
            ) {
                Text(
                    text = if (questionIndex + 1 < totalQuestions) "Soal Berikutnya 👉" else "Lihat Hasil Teka-Teki 🏆",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        } else {
            // Big Submit Answer Button
            Button(
                onClick = onSubmitAnswer,
                enabled = selectedOption != null,
                colors = ButtonDefaults.buttonColors(containerColor = SunnyOrange),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("quiz_check_answer_btn")
            ) {
                Text(
                    text = "Kunci Jawaban! 🎯",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun StudentQuizResultScreen(
    puzzle: PuzzlePackage,
    studentName: String,
    score: Int,
    total: Int,
    onGenerateResultQr: () -> Unit,
    onPlayAgain: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage = if (total > 0) ((score.toFloat() / total) * 100).toInt() else 0
    val starCount = when {
        total == 0 -> 1
        score == total -> 3
        score >= (total / 2) -> 2
        else -> 1
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("student_quiz_result_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Celebration Trophy
        Text(text = "🏆", fontSize = 72.sp)

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Hebat Sekali, $studentName! 🎉",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = SunnyOrangeDark,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Kamu telah menyelesaikan \"${puzzle.title}\"",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Big Star Row
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..3) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (i <= starCount) SunnyYellow else Color(0xFFE0E0E0),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Score Card
        ElevatedCard(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Skor Akhir Kamu",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                Text(
                    text = "$score / $total",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GrassGreen
                )
                Text(
                    text = "$percentage% Jawaban Benar",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Primary Feature: Generate QR Code for Teacher to scan
        Button(
            onClick = onGenerateResultQr,
            colors = ButtonDefaults.buttonColors(containerColor = SunnyOrange),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .testTag("generate_student_result_qr_btn")
        ) {
            Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Buat QR Nilai untuk Bu Guru 📱",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onPlayAgain,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("play_again_btn")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Main Lagi", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onBackToHome,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("back_to_student_home_btn")
            ) {
                Text("Pilih Soal Lain", color = Color(0xFF37474F), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
