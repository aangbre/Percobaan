package com.example.ui.teacher

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnimalQuestion
import com.example.data.model.PuzzlePackage
import com.example.data.model.StudentSubmission
import com.example.ui.EduViewModel
import com.example.ui.ScanPurpose
import com.example.ui.theme.GrassGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SunnyOrange
import com.example.ui.theme.SunnyYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboard(
    viewModel: EduViewModel,
    puzzles: List<PuzzlePackage>,
    submissions: List<StudentSubmission>,
    onOpenScanner: (ScanPurpose) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Daftar Siswa", "Bank Soal & QR", "Buat Soal")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("teacher_dashboard")
    ) {
        // Teacher Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = OceanBlue),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👩‍🏫", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Dasbor Guru TK",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Mode Offline • Pertukaran QR Code",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Button(
                    onClick = { onOpenScanner(ScanPurpose.TEACHER_SCAN_RESULT) },
                    colors = ButtonDefaults.buttonColors(containerColor = SunnyYellow),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("teacher_scan_student_qr_header_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Pindai QR Siswa",
                        tint = Color(0xFF2E241E),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scan QR",
                        color = Color(0xFF2E241E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Navigation Tabs
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = SunnyOrange,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val icon = when (index) {
                    0 -> Icons.Default.People
                    1 -> Icons.Default.MenuBook
                    else -> Icons.Default.AddCircle
                }
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("teacher_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content Area based on selected tab
        when (selectedTab) {
            0 -> StudentListTab(
                submissions = submissions,
                onScanClick = { onOpenScanner(ScanPurpose.TEACHER_SCAN_RESULT) },
                onDelete = { viewModel.deleteSubmission(it) },
                onClearAll = { viewModel.clearAllSubmissions() }
            )
            1 -> PuzzleBankTab(
                puzzles = puzzles,
                onShowQr = { viewModel.showPuzzleQr(it) },
                onDelete = { viewModel.deletePuzzle(it) },
                onCreateNew = { selectedTab = 2 }
            )
            2 -> CreatePuzzleTab(
                onCreatePuzzle = { title, desc, author, questions ->
                    viewModel.createPuzzle(title, desc, author, questions)
                    selectedTab = 1
                }
            )
        }
    }
}

@Composable
fun StudentListTab(
    submissions: List<StudentSubmission>,
    onScanClick: () -> Unit,
    onDelete: (Long) -> Unit,
    onClearAll: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("student_list_tab")
    ) {
        // Summary & Scan CTA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daftar Nilai Siswa",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${submissions.size} data hasil pengerjaan tersimpan",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Row {
                if (submissions.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearAll,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("clear_all_submissions_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Semua", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = onScanClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GrassGreen),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("scan_student_submission_btn")
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pindai QR Siswa", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        if (submissions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(text = "🎒", fontSize = 54.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum Ada Nilai Siswa",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Siswa yang selesai mengerjakan teka-teki akan membuat QR Code. Gunakan tombol 'Pindai QR Siswa' untuk mencatat nilainya!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onScanClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GrassGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mulai Pindai QR Nilai")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(submissions, key = { it.id }) { item ->
                    StudentItemCard(
                        item = item,
                        formattedDate = dateFormat.format(Date(item.timestamp)),
                        onDelete = { onDelete(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentItemCard(
    item: StudentSubmission,
    formattedDate: String,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(SunnyYellow.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🧒", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.studentName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.puzzleTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = OceanBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Score Box
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${item.score}/${item.totalQuestions}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = GrassGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = SunnyOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${item.percentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_student_${item.id}_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Nilai",
                        tint = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun PuzzleBankTab(
    puzzles: List<PuzzlePackage>,
    onShowQr: (PuzzlePackage) -> Unit,
    onDelete: (String) -> Unit,
    onCreateNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("puzzle_bank_tab")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bank Teka-Teki Hewan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Buat QR Code untuk dibagikan ke siswa tanpa internet",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onCreateNew,
                colors = ButtonDefaults.buttonColors(containerColor = SunnyOrange),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("create_new_puzzle_top_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Buat Soal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(puzzles, key = { it.id }) { puzzle ->
                PuzzleItemCard(
                    puzzle = puzzle,
                    onShowQr = { onShowQr(puzzle) },
                    onDelete = { onDelete(puzzle.id) }
                )
            }
        }
    }
}

@Composable
fun PuzzleItemCard(
    puzzle: PuzzlePackage,
    onShowQr: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("puzzle_card_${puzzle.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(SunnyYellow.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = puzzle.questions.firstOrNull()?.animalEmoji ?: "🦁",
                            fontSize = 26.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = puzzle.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${puzzle.questions.size} Soal Logika Hewan • Oleh ${puzzle.author}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                if (!puzzle.id.startsWith("preset_")) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_puzzle_${puzzle.id}_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Teka-Teki",
                            tint = Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = puzzle.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action: Generate QR Code for this puzzle
            Button(
                onClick = onShowQr,
                colors = ButtonDefaults.buttonColors(containerColor = SunnyOrange),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_qr_for_puzzle_${puzzle.id}_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tampilkan QR Code Soal 📱",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CreatePuzzleTab(
    onCreatePuzzle: (title: String, desc: String, author: String, questions: List<AnimalQuestion>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("Guru TK") }

    // List of questions being created
    val questionList = remember {
        mutableStateListOf(
            AnimalQuestion(
                id = "q_1",
                question = "Hewan apa yang suka makan rumput dan menghasilkan susu? 🐄",
                animalEmoji = "🐮",
                options = listOf("Sapi 🐮", "Ayam 🐔", "Ikan 🐟"),
                correctIndex = 0,
                explanation = "Sapi suka makan rumput dan susunya bergizi!",
                soundText = "Moo~!"
            ),
            AnimalQuestion(
                id = "q_2",
                question = "Hewan mana yang pandai memanjat pohon dan suka makan pisang? 🍌",
                animalEmoji = "🐵",
                options = listOf("Monyet 🐵", "Gajah 🐘", "Bebek 🦆"),
                correctIndex = 0,
                explanation = "Monyet lincah melompat di dahan pohon!",
                soundText = "U-u-a-a!"
            )
        )
    }

    // New Question draft state
    var currentQuestionText by remember { mutableStateOf("") }
    var currentEmoji by remember { mutableStateOf("🦁") }
    var opt1 by remember { mutableStateOf("") }
    var opt2 by remember { mutableStateOf("") }
    var opt3 by remember { mutableStateOf("") }
    var correctIndex by remember { mutableIntStateOf(0) }
    var explanation by remember { mutableStateOf("") }

    val emojis = listOf("🦁", "🐘", "🐮", "🐶", "🐱", "🐰", "🐬", "🦆", "🦉", "🐵", "🦒", "🐸")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("create_puzzle_tab")
    ) {
        Text(
            text = "Buat Teka-Teki Logika Baru ✍️",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Setelah dibuat, Anda dapat menampilkan QR code untuk dipindai siswa.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Teka-Teki") },
                    placeholder = { Text("Contoh: Sahabat Rimba Ceria 🦁") },
                    modifier = Modifier.fillMaxWidth().testTag("puzzle_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Singkat") },
                    placeholder = { Text("Contoh: Teka-teki makanan dan suara hewan") },
                    modifier = Modifier.fillMaxWidth().testTag("puzzle_desc_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Nama Pembuat") },
                    placeholder = { Text("Contoh: Bu Guru Maya") },
                    modifier = Modifier.fillMaxWidth().testTag("puzzle_author_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Existing questions count
        Text(
            text = "Daftar Pertanyaan (${questionList.size} Soal):",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        questionList.forEachIndexed { idx, q ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(text = q.animalEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${idx + 1}. ${q.question}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Kunci: ${q.options.getOrNull(q.correctIndex) ?: ""}",
                                fontSize = 11.sp,
                                color = GrassGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (questionList.size > 1) {
                        IconButton(onClick = { questionList.removeAt(idx) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Soal", tint = Color.LightGray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form to add a new question
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tambah Pertanyaan Baru ➕",
                    fontWeight = FontWeight.Bold,
                    color = SunnyOrange
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Emoji picker row
                Text(text = "Pilih Maskot Hewan:", fontSize = 12.sp, color = Color.Gray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojis.take(6).forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentEmoji == emoji) SunnyYellow else Color(0xFFF0F0F0))
                                .clickable { currentEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = currentQuestionText,
                    onValueChange = { currentQuestionText = it },
                    label = { Text("Teks Pertanyaan Teka-Teki") },
                    placeholder = { Text("Contoh: Hewan apa yang punya cangkang keras? 🐢") },
                    modifier = Modifier.fillMaxWidth().testTag("new_question_text_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Pilihan Jawaban (Tandai Kunci Jawaban):", fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(6.dp))

                // Option 1
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = correctIndex == 0,
                        onClick = { correctIndex = 0 },
                        colors = RadioButtonDefaults.colors(selectedColor = GrassGreen)
                    )
                    OutlinedTextField(
                        value = opt1,
                        onValueChange = { opt1 = it },
                        placeholder = { Text("Pilihan 1 (Contoh: Kura-kura 🐢)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Option 2
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = correctIndex == 1,
                        onClick = { correctIndex = 1 },
                        colors = RadioButtonDefaults.colors(selectedColor = GrassGreen)
                    )
                    OutlinedTextField(
                        value = opt2,
                        onValueChange = { opt2 = it },
                        placeholder = { Text("Pilihan 2 (Contoh: Kucing 🐱)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Option 3
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = correctIndex == 2,
                        onClick = { correctIndex = 2 },
                        colors = RadioButtonDefaults.colors(selectedColor = GrassGreen)
                    )
                    OutlinedTextField(
                        value = opt3,
                        onValueChange = { opt3 = it },
                        placeholder = { Text("Pilihan 3 (Contoh: Burung 🕊️)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Pesan Penjelasan / Petunjuk untuk Anak") },
                    placeholder = { Text("Contoh: Kura-kura berlindung di dalam tempurungnya!") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        if (currentQuestionText.isNotBlank() && opt1.isNotBlank() && opt2.isNotBlank()) {
                            val options = listOf(opt1, opt2, if (opt3.isNotBlank()) opt3 else "Lainnya")
                            questionList.add(
                                AnimalQuestion(
                                    id = "q_${System.currentTimeMillis()}",
                                    question = currentQuestionText,
                                    animalEmoji = currentEmoji,
                                    options = options,
                                    correctIndex = correctIndex.coerceIn(0, options.size - 1),
                                    explanation = explanation
                                )
                            )
                            // Reset inputs
                            currentQuestionText = ""
                            opt1 = ""
                            opt2 = ""
                            opt3 = ""
                            explanation = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_question_to_list_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambahkan Soal Ini ke Daftar")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Button
        Button(
            onClick = {
                val finalTitle = title.ifBlank { "Teka-Teki Ceria TK 🦁" }
                val finalDesc = description.ifBlank { "Logika dan pengetahuan hewan" }
                val finalAuthor = author.ifBlank { "Bu Guru" }
                onCreatePuzzle(finalTitle, finalDesc, finalAuthor, questionList.toList())
            },
            colors = ButtonDefaults.buttonColors(containerColor = SunnyOrange),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("save_puzzle_btn")
        ) {
            Icon(Icons.Default.AddCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Simpan Teka-Teki & Siapkan QR 🌟",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
