package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AnimalQuestion
import com.example.data.model.PuzzlePackage
import com.example.data.model.StudentSubmission
import com.example.ui.ScanPurpose
import com.example.ui.theme.GrassGreen
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SunnyOrange
import com.example.util.QrCodeUtil

@Composable
fun QrScannerDialog(
    purpose: ScanPurpose,
    onScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var manualInput by remember { mutableStateOf("") }

    // Scanner laser animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_anim"
    )

    val titleText = when (purpose) {
        ScanPurpose.TEACHER_SCAN_RESULT -> "Pindai QR Hasil Nilai Siswa 🎓"
        ScanPurpose.STUDENT_SCAN_PUZZLE -> "Pindai QR Teka-Teki Guru 🦁"
    }

    val guideText = when (purpose) {
        ScanPurpose.TEACHER_SCAN_RESULT -> "Arahkan kamera ke layar HP siswa untuk mencatat nilai secara offline."
        ScanPurpose.STUDENT_SCAN_PUZZLE -> "Arahkan kamera ke lembar/layar QR Guru untuk membuka teka-teki baru."
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(16.dp)
                .testTag("qr_scanner_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_scanner_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup Scanner",
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = guideText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Camera viewfinder viewfinder box
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(3.dp, SunnyOrange, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(90.dp),
                        tint = Color(0x55FFFFFF)
                    )

                    // Animated laser line
                    Box(
                        modifier = Modifier
                            .offset(y = (laserOffsetY - 90).dp)
                            .fillMaxWidth(0.85f)
                            .height(3.dp)
                            .background(SunnyOrange)
                    )

                    Text(
                        text = "Kamera Aktif (Mode Offline)",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Test / Simulation shortcuts for instant emulator verification
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C34)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = SunnyOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Uji Coba Cepat (Simulasi Scan QR)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (purpose == ScanPurpose.TEACHER_SCAN_RESULT) {
                            Button(
                                onClick = {
                                    val simulatedResult = StudentSubmission(
                                        studentName = "Fathir (TK-B)",
                                        puzzleTitle = "Teka-Teki Sahabat Hewan 🦁",
                                        score = 4,
                                        totalQuestions = 4,
                                        stars = 3,
                                        timestamp = System.currentTimeMillis(),
                                        notes = "Hasil scan QR offline siswa"
                                    )
                                    onScanned(QrCodeUtil.serializeStudentResult(simulatedResult))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GrassGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("simulate_scan_fathir_btn")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simulasi Scan: Nilai Fathir (4/4 ⭐)")
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = {
                                    val simulatedResult = StudentSubmission(
                                        studentName = "Nadia Citra (TK-A)",
                                        puzzleTitle = "Hewan Air & Langit 🐬",
                                        score = 3,
                                        totalQuestions = 3,
                                        stars = 3,
                                        timestamp = System.currentTimeMillis(),
                                        notes = "Siswa sangat antusias"
                                    )
                                    onScanned(QrCodeUtil.serializeStudentResult(simulatedResult))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("simulate_scan_nadia_btn")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simulasi Scan: Nilai Nadia (3/3 ⭐)")
                            }
                        } else {
                            Button(
                                onClick = {
                                    val customSimulatedPuzzle = PuzzlePackage(
                                        id = "sim_${System.currentTimeMillis()}",
                                        title = "Teka-Teki Rimba Ceria 🌴",
                                        description = "Teka-teki seru dari QR Guru",
                                        author = "Bu Guru Maya",
                                        questions = listOf(
                                            AnimalQuestion(
                                                id = "sq1",
                                                question = "Hewan apa yang punya leher sangat tinggi dan makan daun pucuk? 🦒",
                                                animalEmoji = "🦒",
                                                options = listOf("Jerapah 🦒", "Kucing 🐱", "Bebek 🦆"),
                                                correctIndex = 0,
                                                explanation = "Jerapah adalah hewan tertinggi di darat!",
                                                soundText = "Krrk-krrk!"
                                            ),
                                            AnimalQuestion(
                                                id = "sq2",
                                                question = "Siapa raja hutan yang memiliki surai lebat dan auman keras? 🦁",
                                                animalEmoji = "🦁",
                                                options = listOf("Singa 🦁", "Tikus 🐭", "Ayam 🐓"),
                                                correctIndex = 0,
                                                explanation = "Singa mengaum sangat keras 'ROAAAR'!",
                                                soundText = "ROAAAR!"
                                            )
                                        )
                                    )
                                    onScanned(QrCodeUtil.serializePuzzle(customSimulatedPuzzle))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SunnyOrange),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("simulate_scan_puzzle_btn")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simulasi Scan: Buka Soal Baru 'Rimba Ceria'")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFF3E3E48))
                Spacer(modifier = Modifier.height(14.dp))

                // Manual Input option
                Text(
                    text = "Atau Tempel / Masukkan Kode QR Manual:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    placeholder = { Text("Tempel JSON / Kode QR di sini...", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_qr_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        if (manualInput.isNotBlank()) {
                            onScanned(manualInput.trim())
                        }
                    },
                    enabled = manualInput.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("submit_manual_qr_btn")
                ) {
                    Text("Proses Kode QR", color = if (manualInput.isNotBlank()) SunnyOrange else Color.Gray)
                }
            }
        }
    }
}
