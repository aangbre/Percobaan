package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.QrDisplayDialog
import com.example.ui.components.QrScannerDialog
import com.example.ui.student.StudentDashboard
import com.example.ui.teacher.TeacherDashboard
import com.example.ui.theme.GrassGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.SunnyOrange
import com.example.ui.theme.SunnyOrangeDark
import com.example.ui.theme.SunnyYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduApp(viewModel: EduViewModel) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val puzzles by viewModel.puzzles.collectAsStateWithLifecycle()
    val submissions by viewModel.submissions.collectAsStateWithLifecycle()

    // Student Quiz States
    val studentName by viewModel.studentName.collectAsStateWithLifecycle()
    val activePuzzle by viewModel.activePuzzle.collectAsStateWithLifecycle()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val selectedOption by viewModel.selectedOption.collectAsStateWithLifecycle()
    val isAnswerSubmitted by viewModel.isAnswerSubmitted.collectAsStateWithLifecycle()
    val currentScore by viewModel.currentScore.collectAsStateWithLifecycle()
    val isQuizFinished by viewModel.isQuizFinished.collectAsStateWithLifecycle()

    // Dialog States
    val qrDisplayData by viewModel.qrDisplayData.collectAsStateWithLifecycle()
    val scannerPurpose by viewModel.scannerPurpose.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (currentRole == UserRole.STUDENT) "🦁 EduTK Ceria" else "👩‍🏫 Dasbor Guru TK",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF2E241E)
                        )
                    }
                },
                actions = {
                    // Quick Role Switcher Button
                    Button(
                        onClick = {
                            viewModel.switchRole(
                                if (currentRole == UserRole.STUDENT) UserRole.TEACHER else UserRole.STUDENT
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentRole == UserRole.STUDENT) OceanBlue else SunnyOrange
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("switch_role_btn")
                    ) {
                        Text(
                            text = if (currentRole == UserRole.STUDENT) "Mode Guru 👩‍🏫" else "Mode Siswa 🎒",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SunnyYellow.copy(alpha = 0.4f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Banner alert notification if any
                AnimatedVisibility(
                    visible = bannerMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    bannerMessage?.let { msg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = GrassGreen)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = msg,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.clearBanner() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Tutup",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Main screen depending on active role
                when (currentRole) {
                    UserRole.STUDENT -> {
                        StudentDashboard(
                            viewModel = viewModel,
                            puzzles = puzzles,
                            activePuzzle = activePuzzle,
                            currentQuestionIndex = currentQuestionIndex,
                            selectedOption = selectedOption,
                            isAnswerSubmitted = isAnswerSubmitted,
                            currentScore = currentScore,
                            isQuizFinished = isQuizFinished,
                            studentName = studentName,
                            onOpenScanner = { purpose -> viewModel.openScanner(purpose) }
                        )
                    }
                    UserRole.TEACHER -> {
                        TeacherDashboard(
                            viewModel = viewModel,
                            puzzles = puzzles,
                            submissions = submissions,
                            onOpenScanner = { purpose -> viewModel.openScanner(purpose) }
                        )
                    }
                }
            }

            // QR Display Dialog (when user generates QR for puzzle or student result)
            qrDisplayData?.let { data ->
                QrDisplayDialog(
                    data = data,
                    onDismiss = { viewModel.dismissQrDialog() }
                )
            }

            // QR Scanner Dialog (when user taps Scan QR)
            scannerPurpose?.let { purpose ->
                QrScannerDialog(
                    purpose = purpose,
                    onScanned = { rawPayload -> viewModel.onQrScanned(rawPayload) },
                    onDismiss = { viewModel.closeScanner() }
                )
            }
        }
    }
}
