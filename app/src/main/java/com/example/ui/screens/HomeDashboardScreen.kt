package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AssignmentEntity
import com.example.data.NoteEntity
import com.example.data.PdfBookEntity
import com.example.data.StudyTaskEntity
import com.example.data.SubjectEntity
import com.example.data.UserPreferences
import com.example.ui.components.SubjectBadgePill
import com.example.ui.components.WolfCard
import com.example.ui.components.WolfLogoEmblem
import com.example.ui.theme.BrightGold
import com.example.ui.theme.BurnishedGold
import com.example.ui.theme.EmeraldSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val MOTIVATIONAL_QUOTES = listOf(
    "I'M HUNGRY. GIVE ME MORE.",
    "STUDY SMART. STAY HUNGRY.",
    "DISCIPLINE OUTLASTS MOTIVATION. KEEP HUNTING KNOWLEDGE.",
    "ONE CHAPTER DEEPER. ONE STEP STRONGER.",
    "THE LONE WOLF PREPARES WHILE OTHERS REST."
)

@Composable
fun HomeDashboardScreen(
    preferences: UserPreferences,
    subjects: List<SubjectEntity>,
    notes: List<NoteEntity>,
    pdfBooks: List<PdfBookEntity>,
    tasks: List<StudyTaskEntity>,
    assignments: List<AssignmentEntity>,
    onOpenSubjects: () -> Unit,
    onOpenSubjectDetail: (Int) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenNoteEditor: (Long) -> Unit,
    onOpenDiary: () -> Unit,
    onOpenTimetable: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    onOpenAiSearch: (String?) -> Unit = {},
    onOpenBookScanner: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val todayLabel = remember {
        SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())
    }
    var quoteIndex by remember { mutableIntStateOf(0) }

    val completedTasks = tasks.count { it.isCompleted }
    val totalTasks = tasks.size
    val completedAssignments = assignments.count { it.status == "COMPLETED" }
    val totalWorkItems = totalTasks + assignments.size
    val completedWorkItems = completedTasks + completedAssignments
    val studyProgress = if (totalWorkItems == 0) {
        if (notes.isNotEmpty() || pdfBooks.isNotEmpty()) 0.35f else 0.15f
    } else {
        (completedWorkItems.toFloat() / totalWorkItems.toFloat()).coerceIn(0f, 1f)
    }
    val progressPercent = (studyProgress * 100).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Brand Header + Welcome + Today's Date
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    WolfLogoEmblem(size = 52.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "WOLF STUDY",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Welcome, ${preferences.studentName}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = todayLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("dashboard_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.cd_settings),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // AI Search Bar Entry Point
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .clickable { onOpenAiSearch(null) }
                    .testTag("dashboard_ai_search_bar"),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "AI Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AI Search topics, exam Qs & your notes...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "AI SEARCH",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Motivational Quote Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.65f), MaterialTheme.colorScheme.outline)),
                        shape = MaterialTheme.shapes.large
                    )
                    .clickable { quoteIndex = (quoteIndex + 1) % MOTIVATIONAL_QUOTES.size }
                    .padding(16.dp)
                    .testTag("dashboard_quote_card")
            ) {
                Column {
                    Text(
                        text = "DAILY FUEL • TAP FOR NEXT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${MOTIVATIONAL_QUOTES[quoteIndex]}\"",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Study Progress & Core Metrics Row
        item {
            WolfCard(borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "STUDY PROGRESS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (totalWorkItems == 0) {
                                "Ready to conquer your 7 subjects"
                            } else {
                                "$completedWorkItems of $totalWorkItems tasks & assignments done"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            DashboardStatChip(
                                label = "Subjects",
                                value = "${subjects.size}",
                                onClick = onOpenSubjects
                            )
                            DashboardStatChip(
                                label = "Done Tasks",
                                value = "$completedTasks/$totalTasks",
                                onClick = onOpenTasks
                            )
                            DashboardStatChip(
                                label = "Notes",
                                value = "${notes.size}",
                                onClick = onOpenNotes
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier.size(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { studyProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            strokeWidth = 7.dp
                        )
                        Text(
                            text = "$progressPercent%",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quick Access Buttons Grid (All core modules reachable in 1 tap)
        item {
            Text(
                text = "QUICK ACCESS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            val quickActions = listOf(
                QuickAccessItem("AI Search", Icons.Default.ManageSearch, "quick_ai_search", { onOpenAiSearch(null) }),
                QuickAccessItem("7 Subjects", Icons.Default.MenuBook, "quick_subjects", onOpenSubjects),
                QuickAccessItem("PDF Library", Icons.Default.LibraryBooks, "quick_library", onOpenLibrary),
                QuickAccessItem("My Notes", Icons.Default.EditNote, "quick_notes", onOpenNotes),
                QuickAccessItem("AI Tutor", Icons.Default.AutoAwesome, "quick_ai", onOpenAiAssistant),
                QuickAccessItem("Book Scanner", Icons.Default.DocumentScanner, "quick_scanner", onOpenBookScanner),
                QuickAccessItem("Study Diary", Icons.AutoMirrored.Filled.MenuBook, "quick_diary", onOpenDiary),
                QuickAccessItem("Timetable", Icons.Default.Schedule, "quick_timetable", onOpenTimetable),
                QuickAccessItem("Task Manager", Icons.Default.TaskAlt, "quick_tasks", onOpenTasks),
                QuickAccessItem("Bookmarks", Icons.Default.Bookmark, "quick_bookmarks", onOpenBookmarks),
                QuickAccessItem("Backup", Icons.Default.Backup, "quick_backup", onOpenBackup),
                QuickAccessItem("Settings", Icons.Default.Settings, "quick_settings", onOpenSettings)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                quickActions.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { item ->
                            QuickAccessTile(
                                item = item,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 7 Subjects Horizontal Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR 7 SUBJECTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "View all",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenSubjects)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("dashboard_view_all_subjects")
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(subjects, key = { it.id }) { subject ->
                    val subjectNotes = notes.count { it.subjectId == subject.id }
                    val subjectBooks = pdfBooks.count { it.subjectId == subject.id }
                    Surface(
                        modifier = Modifier
                            .width(172.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onOpenSubjectDetail(subject.id) }
                            .testTag("dashboard_subject_card_${subject.id}"),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color(subject.accentColorHex).copy(alpha = 0.45f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            SubjectBadgePill(subject.shortCode, subject.accentColorHex)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$subjectNotes notes • $subjectBooks PDFs",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Recent Notes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT NOTES",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "+ New Note",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenNoteEditor(0L) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("dashboard_new_note_button")
                )
            }
        }

        if (notes.isEmpty()) {
            item {
                WolfCard(onClick = { onOpenNoteEditor(0L) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Add first note",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Start your first study note",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Tap to write, scan a book page, or ask the AI Tutor.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(notes.take(4), key = { it.id }) { note ->
                val subj = subjects.firstOrNull { it.id == note.subjectId }
                WolfCard(
                    onClick = { onOpenNoteEditor(note.id) },
                    modifier = Modifier.testTag("dashboard_recent_note_${note.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (subj != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            SubjectBadgePill(subj.shortCode, subj.accentColorHex)
                        }
                    }
                    if (note.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Pending Tasks Preview
        val pendingTasks = tasks.filter { !it.isCompleted }.take(3)
        if (pendingTasks.isNotEmpty()) {
            item {
                Text(
                    text = "PENDING TASKS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(pendingTasks, key = { "pending_${it.id}" }) { task ->
                WolfCard(onClick = onOpenTasks) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Pending task",
                                tint = EmeraldSuccess.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Due date",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun DashboardStatChip(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class QuickAccessItem(
    val title: String,
    val icon: ImageVector,
    val tag: String,
    val onClick: () -> Unit
)

@Composable
private fun QuickAccessTile(
    item: QuickAccessItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(64.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = item.onClick)
            .testTag(item.tag),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
