package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AssignmentEntity
import com.example.data.DiaryEntryEntity
import com.example.data.ImportantQuestionEntity
import com.example.data.NoteEntity
import com.example.data.PdfBookEntity
import com.example.data.PdfPageBookmarkEntity
import com.example.data.StudyTaskEntity
import com.example.data.SubjectEntity
import com.example.data.TimetableSlotEntity
import com.example.ui.components.BookmarkIconButton
import com.example.ui.components.SubjectBadgePill
import com.example.ui.components.WolfCard
import com.example.ui.components.WolfEmptyState
import com.example.ui.components.WolfTopBar
import com.example.ui.theme.BrightGold
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldSuccess
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// 8. STUDY DIARY
@Composable
fun StudyDiaryScreen(
    entries: List<DiaryEntryEntity>,
    subjects: List<SubjectEntity>,
    onSaveEntry: (DiaryEntryEntity) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onBack: () -> Unit
) {
    var editingEntry by remember { mutableStateOf<DiaryEntryEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { WolfTopBar(title = "Study Diary", subtitle = "Daily Learning Records", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingEntry = null
                    showDialog = true
                },
                modifier = Modifier.testTag("diary_add_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add diary entry")
            }
        }
    ) { innerPadding ->
        if (entries.isEmpty()) {
            WolfEmptyState(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "No Study Diary Entries",
                subtitle = "Log what you studied today, track study minutes, and preserve your daily reflections.",
                modifier = Modifier.padding(innerPadding),
                actionButton = {
                    Button(onClick = { showDialog = true }) { Text("Write Today's Log") }
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).testTag("study_diary_screen"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    val subj = subjects.firstOrNull { it.id == entry.subjectId }
                    WolfCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${entry.dateKey} • ${entry.studyMinutes} mins",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = {
                                        editingEntry = entry
                                        showDialog = true
                                    },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit)) }
                                IconButton(
                                    onClick = { onDeleteEntry(entry.id) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete)) }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = entry.learningRecord,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (subj != null) {
                                SubjectBadgePill(subj.shortCode, subj.accentColorHex)
                            } else {
                                Spacer(Modifier.width(1.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(entry.focusRating.coerceIn(1, 5)) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = BrightGold,
                                        modifier = Modifier.size(16.dp)
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

    if (showDialog) {
        DiaryEntryDialog(
            initial = editingEntry,
            subjects = subjects,
            onDismiss = { showDialog = false },
            onSave = {
                onSaveEntry(it)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryEntryDialog(
    initial: DiaryEntryEntity?,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (DiaryEntryEntity) -> Unit
) {
    val defaultDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var dateKey by remember { mutableStateOf(initial?.dateKey ?: defaultDate) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var learningRecord by remember { mutableStateOf(initial?.learningRecord ?: "") }
    var studyMinutes by remember { mutableStateOf((initial?.studyMinutes ?: 60).toString()) }
    var focusRating by remember { mutableIntStateOf(initial?.focusRating ?: 4) }
    var selectedSubjectId by remember { mutableStateOf(initial?.subjectId ?: 1) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New Study Diary Entry" else "Edit Diary Entry") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { OutlinedTextField(value = dateKey, onValueChange = { dateKey = it }, label = { Text("Date (yyyy-MM-dd)") }) }
                item { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Session Title") }) }
                item { OutlinedTextField(value = learningRecord, onValueChange = { learningRecord = it }, label = { Text("What did you learn today?") }, minLines = 3) }
                item { OutlinedTextField(value = studyMinutes, onValueChange = { studyMinutes = it.filter { c -> c.isDigit() } }, label = { Text("Study Duration (minutes)") }) }
                item {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        val name = subjects.firstOrNull { it.id == selectedSubjectId }?.name ?: "Education (Major)"
                        OutlinedTextField(
                            value = name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Subject") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            subjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj.name) },
                                    onClick = {
                                        selectedSubjectId = subj.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    Text("Focus Rating", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..5).forEach { rating ->
                            FilterChip(
                                selected = focusRating == rating,
                                onClick = { focusRating = rating },
                                label = { Text("$rating★") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank() || learningRecord.isNotBlank()) {
                    onSave(
                        DiaryEntryEntity(
                            id = initial?.id ?: 0,
                            dateKey = dateKey.trim().ifEmpty { defaultDate },
                            title = title.trim().ifEmpty { "Daily Study Session" },
                            learningRecord = learningRecord.trim(),
                            studyMinutes = studyMinutes.toIntOrNull()?.coerceAtLeast(1) ?: 60,
                            focusRating = focusRating,
                            subjectId = selectedSubjectId
                        )
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// 9. TIMETABLE
@Composable
fun TimetableScreen(
    slots: List<TimetableSlotEntity>,
    subjects: List<SubjectEntity>,
    onSaveSlot: (TimetableSlotEntity) -> Unit,
    onDeleteSlot: (Long) -> Unit,
    onBack: () -> Unit
) {
    val todayDayOfWeek = remember {
        val calDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if (calDay == Calendar.SUNDAY) 7 else calDay - 1 // 1=Mon..7=Sun
    }
    var selectedDay by remember { mutableIntStateOf(todayDayOfWeek) }
    var editingSlot by remember { mutableStateOf<TimetableSlotEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val daySlots = slots.filter { it.dayOfWeek == selectedDay }.sortedBy { it.startMinutes }

    Scaffold(
        topBar = { WolfTopBar(title = "Study Timetable", subtitle = "Daily & Weekly Schedule", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingSlot = null
                    showDialog = true
                },
                modifier = Modifier.testTag("timetable_add_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) { Icon(Icons.Default.Add, contentDescription = "Add schedule slot") }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("timetable_screen")
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items((1..7).toList()) { day ->
                    val label = if (day == todayDayOfWeek) "${dayNames[day - 1]} (Today)" else dayNames[day - 1]
                    FilterChip(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        label = { Text(label) },
                        modifier = Modifier.testTag("timetable_day_$day")
                    )
                }
            }

            if (daySlots.isEmpty()) {
                WolfEmptyState(
                    icon = Icons.Default.Schedule,
                    title = "No Classes or Study Blocks",
                    subtitle = "Plan study blocks for ${dayNames[selectedDay - 1]} across your 7 subjects.",
                    actionButton = {
                        Button(onClick = { showDialog = true }) { Text("Add Study Slot") }
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(daySlots, key = { it.id }) { slot ->
                        val subj = subjects.firstOrNull { it.id == slot.subjectId }
                        WolfCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${formatTime(slot.startMinutes)} – ${formatTime(slot.endMinutes)}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = subj?.name ?: "Subject",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (slot.topicNote.isNotBlank()) {
                                        Text(
                                            text = slot.topicNote,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            editingSlot = slot
                                            showDialog = true
                                        },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit)) }
                                    IconButton(
                                        onClick = { onDeleteSlot(slot.id) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete)) }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        TimetableSlotDialog(
            initial = editingSlot,
            defaultDay = selectedDay,
            subjects = subjects,
            onDismiss = { showDialog = false },
            onSave = {
                onSaveSlot(it)
                showDialog = false
            }
        )
    }
}

private fun formatTime(totalMinutes: Int): String {
    val h24 = (totalMinutes / 60).coerceIn(0, 23)
    val min = (totalMinutes % 60).coerceIn(0, 59)
    val amPm = if (h24 >= 12) "PM" else "AM"
    val h12 = when {
        h24 == 0 -> 12
        h24 > 12 -> h24 - 12
        else -> h24
    }
    return String.format(Locale.getDefault(), "%02d:%02d %s", h12, min, amPm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimetableSlotDialog(
    initial: TimetableSlotEntity?,
    defaultDay: Int,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (TimetableSlotEntity) -> Unit
) {
    var selectedSubjectId by remember { mutableIntStateOf(initial?.subjectId ?: 1) }
    var dayOfWeek by remember { mutableIntStateOf(initial?.dayOfWeek ?: defaultDay) }
    var startHour by remember { mutableStateOf(((initial?.startMinutes ?: 540) / 60).toString()) }
    var startMin by remember { mutableStateOf(((initial?.startMinutes ?: 540) % 60).toString().padStart(2, '0')) }
    var endHour by remember { mutableStateOf(((initial?.endMinutes ?: 600) / 60).toString()) }
    var endMin by remember { mutableStateOf(((initial?.endMinutes ?: 600) % 60).toString().padStart(2, '0')) }
    var topicNote by remember { mutableStateOf(initial?.topicNote ?: "") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Timetable Slot" else "Edit Timetable Slot") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    val name = subjects.firstOrNull { it.id == selectedSubjectId }?.name ?: ""
                    OutlinedTextField(
                        value = name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        subjects.forEach { subj ->
                            DropdownMenuItem(
                                text = { Text(subj.name) },
                                onClick = {
                                    selectedSubjectId = subj.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { startHour = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Start Hr (0-23)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startMin,
                        onValueChange = { startMin = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = { endHour = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("End Hr (0-23)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endMin,
                        onValueChange = { endMin = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = topicNote,
                    onValueChange = { topicNote = it },
                    label = { Text("Topic / Room (optional)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val sMin = (startHour.toIntOrNull()?.coerceIn(0, 23) ?: 9) * 60 +
                    (startMin.toIntOrNull()?.coerceIn(0, 59) ?: 0)
                val eMin = (endHour.toIntOrNull()?.coerceIn(0, 23) ?: 10) * 60 +
                    (endMin.toIntOrNull()?.coerceIn(0, 59) ?: 0)
                onSave(
                    TimetableSlotEntity(
                        id = initial?.id ?: 0,
                        subjectId = selectedSubjectId,
                        dayOfWeek = dayOfWeek,
                        startMinutes = sMin,
                        endMinutes = maxOf(sMin + 15, eMin),
                        topicNote = topicNote.trim()
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// 10. TASK MANAGER
@Composable
fun TaskManagerScreen(
    tasks: List<StudyTaskEntity>,
    subjects: List<SubjectEntity>,
    onSaveTask: (StudyTaskEntity) -> Unit,
    onToggleTask: (StudyTaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Pending, 1=Completed
    var showDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<StudyTaskEntity?>(null) }

    val filtered = tasks.filter { if (selectedTab == 0) !it.isCompleted else it.isCompleted }
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = { WolfTopBar(title = "Task Manager", subtitle = "${tasks.count { !it.isCompleted }} pending • ${tasks.count { it.isCompleted }} done", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTask = null
                    showDialog = true
                },
                modifier = Modifier.testTag("task_add_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) { Icon(Icons.Default.Add, contentDescription = "Add task") }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("task_manager_screen")
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Pending (${tasks.count { !it.isCompleted }})") },
                    modifier = Modifier.testTag("tasks_tab_pending")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Completed (${tasks.count { it.isCompleted }})") },
                    modifier = Modifier.testTag("tasks_tab_completed")
                )
            }

            if (filtered.isEmpty()) {
                WolfEmptyState(
                    icon = Icons.Default.TaskAlt,
                    title = if (selectedTab == 0) "All Caught Up!" else "No Completed Tasks Yet",
                    subtitle = if (selectedTab == 0) "Add your study deadlines and targets." else "Complete tasks to build momentum.",
                    actionButton = if (selectedTab == 0) {
                        { Button(onClick = { showDialog = true }) { Text("Add Study Task") } }
                    } else null
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { task ->
                        val subj = subjects.firstOrNull { it.id == task.subjectId }
                        WolfCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { onToggleTask(task) },
                                    modifier = Modifier.testTag("task_checkbox_${task.id}")
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            editingTask = task
                                            showDialog = true
                                        }
                                ) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (subj != null) {
                                            SubjectBadgePill(subj.shortCode, subj.accentColorHex)
                                        }
                                        Text(
                                            text = "Due ${dateFmt.format(Date(task.deadlineMillis))} • ${task.priority}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (task.priority == "HIGH" && !task.isCompleted) CoralError else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { onDeleteTask(task.id) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete)) }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        TaskDialog(
            initial = editingTask,
            subjects = subjects,
            onDismiss = { showDialog = false },
            onSave = {
                onSaveTask(it)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDialog(
    initial: StudyTaskEntity?,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (StudyTaskEntity) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var subjectId by remember { mutableIntStateOf(initial?.subjectId ?: 1) }
    var daysUntilDue by remember { mutableStateOf("2") }
    var priority by remember { mutableStateOf(initial?.priority ?: "MEDIUM") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Study Task" else "Edit Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task Title") })
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    val name = subjects.firstOrNull { it.id == subjectId }?.name ?: ""
                    OutlinedTextField(
                        value = name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        subjects.forEach { subj ->
                            DropdownMenuItem(
                                text = { Text(subj.name) },
                                onClick = {
                                    subjectId = subj.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                if (initial == null) {
                    OutlinedTextField(
                        value = daysUntilDue,
                        onValueChange = { daysUntilDue = it.filter { c -> c.isDigit() } },
                        label = { Text("Deadline in (days)") }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("HIGH", "MEDIUM", "LOW").forEach { p ->
                        FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(p) })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    val deadline = initial?.deadlineMillis
                        ?: (System.currentTimeMillis() + (daysUntilDue.toLongOrNull() ?: 2L) * 86_400_000L)
                    onSave(
                        StudyTaskEntity(
                            id = initial?.id ?: 0,
                            subjectId = subjectId,
                            title = title.trim(),
                            deadlineMillis = deadline,
                            isCompleted = initial?.isCompleted ?: false,
                            priority = priority
                        )
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// 11. BOOKMARKS VAULT
@Composable
fun BookmarksScreen(
    subjects: List<SubjectEntity>,
    notes: List<NoteEntity>,
    pdfBooks: List<PdfBookEntity>,
    pageBookmarks: List<PdfPageBookmarkEntity>,
    importantQuestions: List<ImportantQuestionEntity>,
    assignments: List<AssignmentEntity>,
    onOpenNote: (Long) -> Unit,
    onOpenPdfAtPage: (Long, Int) -> Unit,
    onOpenSubject: (Int) -> Unit,
    onToggleNoteBookmark: (NoteEntity) -> Unit,
    onTogglePdfBookmark: (PdfBookEntity) -> Unit,
    onRemovePageBookmark: (Long) -> Unit,
    onToggleQuestionBookmark: (ImportantQuestionEntity) -> Unit,
    onBack: () -> Unit
) {
    val bookmarkedNotes = notes.filter { it.isBookmarked }
    val bookmarkedBooks = pdfBooks.filter { it.isBookmarked }
    val bookmarkedQuestions = importantQuestions.filter { it.isBookmarked }
    val bookmarkedAssignments = assignments.filter { it.isBookmarked }

    val totalCount = bookmarkedNotes.size + bookmarkedBooks.size + pageBookmarks.size +
        bookmarkedQuestions.size + bookmarkedAssignments.size

    Scaffold(
        topBar = { WolfTopBar(title = "Bookmarks", subtitle = "$totalCount saved items", onBack = onBack) }
    ) { innerPadding ->
        if (totalCount == 0) {
            WolfEmptyState(
                icon = Icons.Default.Bookmark,
                title = "No Bookmarks Saved",
                subtitle = "Tap the bookmark icon on any Note, PDF Book, PDF Page, or Important Question to save it here.",
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("bookmarks_screen"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (bookmarkedNotes.isNotEmpty()) {
                    item {
                        Text("BOOKMARKED NOTES", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    items(bookmarkedNotes, key = { "bm_note_${it.id}" }) { note ->
                        val subj = subjects.firstOrNull { it.id == note.subjectId }
                        WolfCard(onClick = { onOpenNote(note.id) }) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(note.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (subj != null) {
                                        Spacer(Modifier.height(4.dp))
                                        SubjectBadgePill(subj.shortCode, subj.accentColorHex)
                                    }
                                }
                                BookmarkIconButton(isBookmarked = true, onClick = { onToggleNoteBookmark(note) })
                            }
                        }
                    }
                }

                if (pageBookmarks.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(6.dp))
                        Text("PDF PAGE BOOKMARKS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    items(pageBookmarks, key = { "bm_page_${it.id}" }) { bm ->
                        val book = pdfBooks.firstOrNull { it.id == bm.bookId }
                        WolfCard(onClick = { if (book != null) onOpenPdfAtPage(book.id, bm.pageNumber) }) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(bm.noteLabel, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "${book?.title ?: "PDF"} • Page ${bm.pageNumber + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(onClick = { onRemovePageBookmark(bm.id) }, modifier = Modifier.minimumInteractiveComponentSize()) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete))
                                }
                            }
                        }
                    }
                }

                if (bookmarkedBooks.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(6.dp))
                        Text("BOOKMARKED PDF BOOKS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    items(bookmarkedBooks, key = { "bm_book_${it.id}" }) { book ->
                        WolfCard(onClick = { onOpenPdfAtPage(book.id, book.lastReadPage) }) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(book.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                BookmarkIconButton(isBookmarked = true, onClick = { onTogglePdfBookmark(book) })
                            }
                        }
                    }
                }

                if (bookmarkedQuestions.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(6.dp))
                        Text("IMPORTANT QUESTIONS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    items(bookmarkedQuestions, key = { "bm_q_${it.id}" }) { q ->
                        WolfCard(onClick = { onOpenSubject(q.subjectId) }) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = EmeraldSuccess)
                                    Spacer(Modifier.width(8.dp))
                                    Text(q.question, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                                BookmarkIconButton(isBookmarked = true, onClick = { onToggleQuestionBookmark(q) })
                            }
                        }
                    }
                }

                if (bookmarkedAssignments.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(6.dp))
                        Text("BOOKMARKED ASSIGNMENTS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    items(bookmarkedAssignments, key = { "bm_a_${it.id}" }) { a ->
                        WolfCard(onClick = { onOpenSubject(a.subjectId) }) {
                            Text(a.title, style = MaterialTheme.typography.titleMedium)
                            Text("Status: ${a.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
