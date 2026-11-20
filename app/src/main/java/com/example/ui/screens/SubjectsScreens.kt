package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AssignmentEntity
import com.example.data.ImportantQuestionEntity
import com.example.data.NoteEntity
import com.example.data.PdfBookEntity
import com.example.data.QuizQuestionEntity
import com.example.data.SubjectEntity
import com.example.ui.components.BookmarkIconButton
import com.example.ui.components.SubjectBadgePill
import com.example.ui.components.WolfCard
import com.example.ui.components.WolfEmptyState
import com.example.ui.components.WolfSearchBar
import com.example.ui.components.WolfTopBar
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubjectsListScreen(
    subjects: List<SubjectEntity>,
    notes: List<NoteEntity>,
    pdfBooks: List<PdfBookEntity>,
    importantQuestions: List<ImportantQuestionEntity>,
    quizQuestions: List<QuizQuestionEntity>,
    assignments: List<AssignmentEntity>,
    onSelectSubject: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("subjects_list_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "7 ACADEMIC SUBJECTS",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap any subject to access Notes, PDF Books, Important Questions, Quiz & Assignments.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(subjects, key = { it.id }) { subject ->
            val nCount = notes.count { it.subjectId == subject.id }
            val bCount = pdfBooks.count { it.subjectId == subject.id }
            val qCount = importantQuestions.count { it.subjectId == subject.id }
            val quizCount = quizQuestions.count { it.subjectId == subject.id }
            val aCount = assignments.count { it.subjectId == subject.id }

            WolfCard(
                borderColor = Color(subject.accentColorHex).copy(alpha = 0.55f),
                onClick = { onSelectSubject(subject.id) },
                modifier = Modifier.testTag("subject_card_${subject.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(subject.accentColorHex).copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${subject.id}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(subject.accentColorHex),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = subject.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(subject.accentColorHex)
                            )
                        }
                    }
                    SubjectBadgePill(subject.shortCode, subject.accentColorHex)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subject.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "$nCount Notes • $bCount PDFs • $qCount Imp. Qs • $quizCount Quiz • $aCount Assignments",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SubjectDetailScreen(
    subject: SubjectEntity,
    notes: List<NoteEntity>,
    pdfBooks: List<PdfBookEntity>,
    importantQuestions: List<ImportantQuestionEntity>,
    quizQuestions: List<QuizQuestionEntity>,
    assignments: List<AssignmentEntity>,
    onBack: () -> Unit,
    onOpenNoteEditor: (Long, Int) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onToggleNoteBookmark: (NoteEntity) -> Unit,
    onImportPdf: (Uri, Int, String?) -> Unit,
    onOpenPdfReader: (Long) -> Unit,
    onDeletePdfBook: (PdfBookEntity) -> Unit,
    onTogglePdfBookmark: (PdfBookEntity) -> Unit,
    onSaveImportantQuestion: (ImportantQuestionEntity) -> Unit,
    onDeleteImportantQuestion: (Long) -> Unit,
    onToggleImportantQuestionBookmark: (ImportantQuestionEntity) -> Unit,
    onSaveQuizQuestion: (QuizQuestionEntity) -> Unit,
    onDeleteQuizQuestion: (Long) -> Unit,
    onRecordQuizAttempt: (Long, Boolean) -> Unit,
    onSaveAssignment: (AssignmentEntity) -> Unit,
    onDeleteAssignment: (Long) -> Unit,
    onToggleAssignmentBookmark: (AssignmentEntity) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    var editingQuestion by remember { mutableStateOf<ImportantQuestionEntity?>(null) }
    var showQuestionDialog by remember { mutableStateOf(false) }

    var editingQuiz by remember { mutableStateOf<QuizQuestionEntity?>(null) }
    var showQuizDialog by remember { mutableStateOf(false) }
    var activeQuizPlayMode by remember { mutableStateOf(false) }

    var editingAssignment by remember { mutableStateOf<AssignmentEntity?>(null) }
    var showAssignmentDialog by remember { mutableStateOf(false) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportPdf(uri, subject.id, null)
        }
    }

    val tabs = listOf("Study Notes", "PDF Books", "Imp. Questions", "Quiz", "Assignments")

    Scaffold(
        topBar = {
            WolfTopBar(
                title = subject.name,
                subtitle = subject.category,
                onBack = onBack
            )
        },
        floatingActionButton = {
            if (!(selectedTab == 3 && activeQuizPlayMode)) {
                FloatingActionButton(
                    onClick = {
                        when (selectedTab) {
                            0 -> onOpenNoteEditor(0L, subject.id)
                            1 -> pdfPickerLauncher.launch(arrayOf("application/pdf"))
                            2 -> {
                                editingQuestion = null
                                showQuestionDialog = true
                            }
                            3 -> {
                                editingQuiz = null
                                showQuizDialog = true
                            }
                            4 -> {
                                editingAssignment = null
                                showAssignmentDialog = true
                            }
                        }
                    },
                    modifier = Modifier.testTag("subject_detail_fab"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = if (selectedTab == 1) Icons.Default.UploadFile else Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_add)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("subject_detail_screen_${subject.id}")
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            activeQuizPlayMode = false
                        },
                        text = { Text(title, maxLines = 1) },
                        modifier = Modifier.testTag("subject_tab_$index")
                    )
                }
            }

            if (!(selectedTab == 3 && activeQuizPlayMode)) {
                WolfSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search in ${subject.name}...",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    testTag = "subject_detail_search"
                )
            }

            val q = searchQuery.trim().lowercase()

            when (selectedTab) {
                // 1. Study Notes
                0 -> {
                    val filtered = notes.filter {
                        q.isEmpty() || it.title.lowercase().contains(q) || it.content.lowercase().contains(q)
                    }
                    if (filtered.isEmpty()) {
                        WolfEmptyState(
                            icon = Icons.Default.NoteAdd,
                            title = "No Study Notes Yet",
                            subtitle = "Create organized notes for ${subject.name}.",
                            actionButton = {
                                Button(onClick = { onOpenNoteEditor(0L, subject.id) }) {
                                    Text("Add Study Note")
                                }
                            }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { note ->
                                WolfCard(onClick = { onOpenNoteEditor(note.id, subject.id) }) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = note.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row {
                                            BookmarkIconButton(
                                                isBookmarked = note.isBookmarked,
                                                onClick = { onToggleNoteBookmark(note) }
                                            )
                                            IconButton(
                                                onClick = { onDeleteNote(note.id) },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.cd_delete),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    if (note.content.isNotBlank()) {
                                        Text(
                                            text = note.content,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }

                // 2. PDF Books
                1 -> {
                    val filtered = pdfBooks.filter {
                        q.isEmpty() || it.title.lowercase().contains(q) || it.fileName.lowercase().contains(q)
                    }
                    if (filtered.isEmpty()) {
                        WolfEmptyState(
                            icon = Icons.Default.PictureAsPdf,
                            title = "No PDF Books Imported",
                            subtitle = "Import PDF textbooks or handouts directly into ${subject.name}.",
                            actionButton = {
                                Button(onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }) {
                                    Text("Import PDF Book")
                                }
                            }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { book ->
                                WolfCard(onClick = { onOpenPdfReader(book.id) }) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PictureAsPdf,
                                                contentDescription = "PDF Book",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(30.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = book.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${book.pageCount} pages • Last page ${book.lastReadPage + 1}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Row {
                                            BookmarkIconButton(
                                                isBookmarked = book.isBookmarked,
                                                onClick = { onTogglePdfBookmark(book) }
                                            )
                                            IconButton(
                                                onClick = { onDeletePdfBook(book) },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.cd_delete),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }

                // 3. Important Questions
                2 -> {
                    val filtered = importantQuestions.filter {
                        q.isEmpty() || it.question.lowercase().contains(q) || it.answer.lowercase().contains(q)
                    }
                    if (filtered.isEmpty()) {
                        WolfEmptyState(
                            icon = Icons.Default.HelpOutline,
                            title = "No Important Questions",
                            subtitle = "Add high-yield exam questions and model answers for ${subject.name}.",
                            actionButton = {
                                Button(onClick = {
                                    editingQuestion = null
                                    showQuestionDialog = true
                                }) {
                                    Text("Add Question")
                                }
                            }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { item ->
                                WolfCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${item.priority} PRIORITY",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Row {
                                            BookmarkIconButton(
                                                isBookmarked = item.isBookmarked,
                                                onClick = { onToggleImportantQuestionBookmark(item) }
                                            )
                                            IconButton(
                                                onClick = {
                                                    editingQuestion = item
                                                    showQuestionDialog = true
                                                },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = stringResource(R.string.cd_edit)
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeleteImportantQuestion(item.id) },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.cd_delete)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = item.question,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    if (item.answer.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = item.answer,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }

                // 4. Quiz (Manage & Interactive Practice Runner)
                3 -> {
                    val filtered = quizQuestions.filter {
                        q.isEmpty() || it.question.lowercase().contains(q)
                    }
                    if (activeQuizPlayMode && quizQuestions.isNotEmpty()) {
                        SubjectQuizRunner(
                            questions = quizQuestions,
                            onRecordAttempt = onRecordQuizAttempt,
                            onExit = { activeQuizPlayMode = false }
                        )
                    } else if (filtered.isEmpty()) {
                        WolfEmptyState(
                            icon = Icons.Default.Quiz,
                            title = "No Quiz Questions Yet",
                            subtitle = "Add MCQ questions manually or build them with the AI Study Assistant.",
                            actionButton = {
                                Button(onClick = {
                                    editingQuiz = null
                                    showQuizDialog = true
                                }) {
                                    Text("Create Quiz Question")
                                }
                            }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Button(
                                    onClick = { activeQuizPlayMode = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("start_subject_quiz_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Start quiz")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Start Practice Quiz (${quizQuestions.size} Qs)")
                                }
                            }
                            items(filtered, key = { it.id }) { quiz ->
                                WolfCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = quiz.question,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    editingQuiz = quiz
                                                    showQuizDialog = true
                                                },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit))
                                            }
                                            IconButton(
                                                onClick = { onDeleteQuizQuestion(quiz.id) },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val opts = listOf(quiz.optionA, quiz.optionB, quiz.optionC, quiz.optionD)
                                    opts.forEachIndexed { idx, opt ->
                                        val isCorrect = idx == quiz.correctIndex
                                        Text(
                                            text = "${'A' + idx}) $opt" + if (isCorrect) " ✓" else "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isCorrect) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }

                // 5. Assignments
                4 -> {
                    val filtered = assignments.filter {
                        q.isEmpty() || it.title.lowercase().contains(q) || it.description.lowercase().contains(q)
                    }
                    if (filtered.isEmpty()) {
                        WolfEmptyState(
                            icon = Icons.Default.Assignment,
                            title = "No Assignments Tracked",
                            subtitle = "Track upcoming submissions and deadlines for ${subject.name}.",
                            actionButton = {
                                Button(onClick = {
                                    editingAssignment = null
                                    showAssignmentDialog = true
                                }) {
                                    Text("Add Assignment")
                                }
                            }
                        )
                    } else {
                        val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { assignment ->
                                WolfCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = assignment.title,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "Due: ${dateFmt.format(Date(assignment.dueDateMillis))} • ${assignment.status}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (assignment.status == "COMPLETED") EmeraldSuccess else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Row {
                                            BookmarkIconButton(
                                                isBookmarked = assignment.isBookmarked,
                                                onClick = { onToggleAssignmentBookmark(assignment) }
                                            )
                                            IconButton(
                                                onClick = {
                                                    editingAssignment = assignment
                                                    showAssignmentDialog = true
                                                },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit))
                                            }
                                            IconButton(
                                                onClick = { onDeleteAssignment(assignment.id) },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete))
                                            }
                                        }
                                    }
                                    if (assignment.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = assignment.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
            }
        }
    }

    if (showQuestionDialog) {
        ImportantQuestionDialog(
            initial = editingQuestion,
            subjectId = subject.id,
            onDismiss = { showQuestionDialog = false },
            onSave = {
                onSaveImportantQuestion(it)
                showQuestionDialog = false
            }
        )
    }

    if (showQuizDialog) {
        QuizQuestionDialog(
            initial = editingQuiz,
            subjectId = subject.id,
            onDismiss = { showQuizDialog = false },
            onSave = {
                onSaveQuizQuestion(it)
                showQuizDialog = false
            }
        )
    }

    if (showAssignmentDialog) {
        AssignmentDialog(
            initial = editingAssignment,
            subjectId = subject.id,
            onDismiss = { showAssignmentDialog = false },
            onSave = {
                onSaveAssignment(it)
                showAssignmentDialog = false
            }
        )
    }
}

@Composable
private fun SubjectQuizRunner(
    questions: List<QuizQuestionEntity>,
    onRecordAttempt: (Long, Boolean) -> Unit,
    onExit: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    if (finished) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Quiz complete",
                tint = EmeraldSuccess,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Quiz Complete!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Score: $score / ${questions.size}",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onExit) { Text("Back to Questions") }
                Button(onClick = {
                    currentIndex = 0
                    selectedOption = -1
                    score = 0
                    finished = false
                }) { Text("Retry Quiz") }
            }
        }
        return
    }

    val current = questions[currentIndex]
    val options = listOf(current.optionA, current.optionB, current.optionC, current.optionD)
    val answered = selectedOption != -1

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${currentIndex + 1} of ${questions.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onExit) { Text("Exit Quiz") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            WolfCard {
                Text(
                    text = current.question,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        items(options.indices.toList()) { idx ->
            val borderColor = when {
                !answered -> MaterialTheme.colorScheme.outline
                idx == current.correctIndex -> EmeraldSuccess
                idx == selectedOption -> CoralError
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(enabled = !answered) {
                        selectedOption = idx
                        val isRight = idx == current.correctIndex
                        if (isRight) score++
                        onRecordAttempt(current.id, isRight)
                    },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.5.dp, borderColor)
            ) {
                Text(
                    text = "${'A' + idx}) ${options[idx]}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (answered) {
            if (current.explanation.isNotBlank()) {
                item {
                    WolfCard(borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) {
                        Text(
                            text = "Explanation",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = current.explanation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        if (currentIndex + 1 < questions.size) {
                            currentIndex++
                            selectedOption = -1
                        } else {
                            finished = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(if (currentIndex + 1 < questions.size) "Next Question" else "View Score")
                }
            }
        }
    }
}

@Composable
private fun ImportantQuestionDialog(
    initial: ImportantQuestionEntity?,
    subjectId: Int,
    onDismiss: () -> Unit,
    onSave: (ImportantQuestionEntity) -> Unit
) {
    var question by remember { mutableStateOf(initial?.question ?: "") }
    var answer by remember { mutableStateOf(initial?.answer ?: "") }
    var priority by remember { mutableStateOf(initial?.priority ?: "HIGH") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Important Question" else "Edit Question") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Question") },
                    minLines = 2
                )
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Answer / Key Points") },
                    minLines = 3
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("HIGH", "MEDIUM", "LOW").forEach { level ->
                        FilterChip(
                            selected = priority == level,
                            onClick = { priority = level },
                            label = { Text(level) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question.isNotBlank()) {
                        onSave(
                            ImportantQuestionEntity(
                                id = initial?.id ?: 0,
                                subjectId = subjectId,
                                question = question.trim(),
                                answer = answer.trim(),
                                priority = priority,
                                isBookmarked = initial?.isBookmarked ?: false
                            )
                        )
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun QuizQuestionDialog(
    initial: QuizQuestionEntity?,
    subjectId: Int,
    onDismiss: () -> Unit,
    onSave: (QuizQuestionEntity) -> Unit
) {
    var question by remember { mutableStateOf(initial?.question ?: "") }
    var optA by remember { mutableStateOf(initial?.optionA ?: "") }
    var optB by remember { mutableStateOf(initial?.optionB ?: "") }
    var optC by remember { mutableStateOf(initial?.optionC ?: "") }
    var optD by remember { mutableStateOf(initial?.optionD ?: "") }
    var correctIdx by remember { mutableIntStateOf(initial?.correctIndex ?: 0) }
    var explanation by remember { mutableStateOf(initial?.explanation ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Quiz MCQ" else "Edit Quiz MCQ") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(value = question, onValueChange = { question = it }, label = { Text("Question") })
                }
                item { OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("Option A") }) }
                item { OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("Option B") }) }
                item { OutlinedTextField(value = optC, onValueChange = { optC = it }, label = { Text("Option C") }) }
                item { OutlinedTextField(value = optD, onValueChange = { optD = it }, label = { Text("Option D") }) }
                item {
                    Text("Correct Option", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("A", "B", "C", "D").forEachIndexed { idx, label ->
                            FilterChip(
                                selected = correctIdx == idx,
                                onClick = { correctIdx = idx },
                                label = { Text(label) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(value = explanation, onValueChange = { explanation = it }, label = { Text("Explanation (optional)") })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (question.isNotBlank() && optA.isNotBlank() && optB.isNotBlank()) {
                    onSave(
                        QuizQuestionEntity(
                            id = initial?.id ?: 0,
                            subjectId = subjectId,
                            question = question.trim(),
                            optionA = optA.trim(),
                            optionB = optB.trim(),
                            optionC = optC.trim().ifEmpty { "None of the above" },
                            optionD = optD.trim().ifEmpty { "All of the above" },
                            correctIndex = correctIdx,
                            explanation = explanation.trim()
                        )
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AssignmentDialog(
    initial: AssignmentEntity?,
    subjectId: Int,
    onDismiss: () -> Unit,
    onSave: (AssignmentEntity) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var daysUntilDue by remember { mutableStateOf("7") }
    var status by remember { mutableStateOf(initial?.status ?: "PENDING") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New Assignment" else "Edit Assignment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Assignment Title") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Details / Instructions") }, minLines = 2)
                if (initial == null) {
                    OutlinedTextField(value = daysUntilDue, onValueChange = { daysUntilDue = it.filter { ch -> ch.isDigit() } }, label = { Text("Due in (days)") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("PENDING", "IN_PROGRESS", "COMPLETED").forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st.replace('_', ' ')) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    val dueMillis = initial?.dueDateMillis
                        ?: (System.currentTimeMillis() + (daysUntilDue.toLongOrNull() ?: 7L) * 86_400_000L)
                    onSave(
                        AssignmentEntity(
                            id = initial?.id ?: 0,
                            subjectId = subjectId,
                            title = title.trim(),
                            description = description.trim(),
                            dueDateMillis = dueMillis,
                            status = status,
                            isBookmarked = initial?.isBookmarked ?: false
                        )
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
