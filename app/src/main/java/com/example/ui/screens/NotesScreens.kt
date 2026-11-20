package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.NoteEntity
import com.example.data.SubjectEntity
import com.example.ui.AutoSaveStatus
import com.example.ui.components.BookmarkIconButton
import com.example.ui.components.SubjectBadgePill
import com.example.ui.components.SubjectFilterRow
import com.example.ui.components.WolfCard
import com.example.ui.components.WolfEmptyState
import com.example.ui.components.WolfSearchBar
import com.example.ui.components.WolfTopBar
import com.example.ui.theme.EmeraldSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesListScreen(
    subjects: List<SubjectEntity>,
    notes: List<NoteEntity>,
    onOpenNoteEditor: (Long, Int?) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onToggleNoteBookmark: (NoteEntity) -> Unit,
    onOpenAiSearch: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    val dateFmt = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    val q = searchQuery.trim().lowercase()
    val filtered = notes.filter { note ->
        val matchesSubject = selectedSubjectId == null || note.subjectId == selectedSubjectId
        val matchesQuery = q.isEmpty() ||
            note.title.lowercase().contains(q) ||
            note.content.lowercase().contains(q)
        matchesSubject && matchesQuery
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenNoteEditor(0L, selectedSubjectId) },
                modifier = Modifier.testTag("notes_add_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New note")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("notes_list_screen")
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "MY NOTES",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                WolfSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search notes by title or content...",
                    testTag = "notes_search_input"
                )
                if (searchQuery.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { onOpenAiSearch(searchQuery.trim()) },
                        modifier = Modifier.fillMaxWidth().testTag("notes_ai_search_button")
                    ) {
                        Icon(Icons.Default.ManageSearch, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI Search \"$searchQuery\"", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            SubjectFilterRow(
                subjects = subjects,
                selectedSubjectId = selectedSubjectId,
                onSelectSubject = { selectedSubjectId = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (filtered.isEmpty()) {
                WolfEmptyState(
                    icon = Icons.Default.EditNote,
                    title = "No Notes Found",
                    subtitle = "Create personal notes that auto-save reliably and stay organized by subject.",
                    actionButton = {
                        Button(onClick = { onOpenNoteEditor(0L, selectedSubjectId) }) {
                            Text("Create Note")
                        }
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { note ->
                        val subject = subjects.firstOrNull { it.id == note.subjectId }
                        WolfCard(
                            onClick = { onOpenNoteEditor(note.id, note.subjectId) },
                            modifier = Modifier.testTag("note_card_${note.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
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
                                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete))
                                    }
                                }
                            }
                            if (note.content.isNotBlank()) {
                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (subject != null) {
                                    SubjectBadgePill(subject.shortCode, subject.accentColorHex)
                                }
                                Text(
                                    text = "${note.sourceTag} • ${dateFmt.format(Date(note.updatedAt))}",
                                    style = MaterialTheme.typography.labelSmall,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    existingNote: NoteEntity?,
    initialSubjectId: Int,
    subjects: List<SubjectEntity>,
    autoSaveStatus: AutoSaveStatus,
    onAutoSave: (Long, Int, String, String, Boolean, String, Long, (Long) -> Unit) -> Unit,
    onSaveImmediate: (NoteEntity, (Long) -> Unit) -> Unit,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit
) {
    var noteId by remember(existingNote?.id) { mutableLongStateOf(existingNote?.id ?: 0L) }
    var subjectId by remember(existingNote?.id) { mutableIntStateOf(existingNote?.subjectId ?: initialSubjectId.coerceIn(1, 7)) }
    var title by remember(existingNote?.id) { mutableStateOf(existingNote?.title ?: "") }
    var content by remember(existingNote?.id) { mutableStateOf(existingNote?.content ?: "") }
    var isBookmarked by remember(existingNote?.id) { mutableStateOf(existingNote?.isBookmarked ?: false) }
    val sourceTag = existingNote?.sourceTag ?: "MANUAL"
    val createdAt = existingNote?.createdAt ?: System.currentTimeMillis()
    var readingMode by remember { mutableStateOf(existingNote != null && existingNote.content.length > 80) }
    var subjectExpanded by remember { mutableStateOf(false) }

    fun triggerAutoSave(nextTitle: String = title, nextContent: String = content, nextSubject: Int = subjectId, nextBookmarked: Boolean = isBookmarked) {
        onAutoSave(
            noteId,
            nextSubject,
            nextTitle,
            nextContent,
            nextBookmarked,
            sourceTag,
            createdAt
        ) { assigned ->
            if (noteId == 0L) noteId = assigned
        }
    }

    Scaffold(
        topBar = {
            WolfTopBar(
                title = if (readingMode) "Read Note" else "Edit Note",
                subtitle = when (autoSaveStatus) {
                    AutoSaveStatus.SAVING -> "Saving..."
                    AutoSaveStatus.SAVED -> "Saved automatically"
                    AutoSaveStatus.IDLE -> "Auto-save ready"
                },
                onBack = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        onSaveImmediate(
                            NoteEntity(
                                id = noteId,
                                subjectId = subjectId,
                                title = title.ifBlank { "Untitled Study Note" },
                                content = content,
                                isBookmarked = isBookmarked,
                                sourceTag = sourceTag,
                                createdAt = createdAt
                            )
                        ) { onBack() }
                    } else {
                        onBack()
                    }
                },
                actions = {
                    BookmarkIconButton(
                        isBookmarked = isBookmarked,
                        onClick = {
                            isBookmarked = !isBookmarked
                            triggerAutoSave(nextBookmarked = isBookmarked)
                        }
                    )
                    IconButton(
                        onClick = { readingMode = !readingMode },
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("note_toggle_read_mode")
                    ) {
                        Icon(
                            imageVector = if (readingMode) Icons.Default.EditNote else Icons.Default.Visibility,
                            contentDescription = if (readingMode) "Switch to edit mode" else "Switch to clean reading mode"
                        )
                    }
                    if (noteId > 0L) {
                        IconButton(
                            onClick = {
                                onDelete(noteId)
                                onBack()
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("note_editor_screen")
        ) {
            if (readingMode) {
                val subj = subjects.firstOrNull { it.id == subjectId }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        if (subj != null) {
                            SubjectBadgePill(subj.name, subj.accentColorHex)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Text(
                            text = title.ifBlank { "Untitled Study Note" },
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    item {
                        WolfCard {
                            Text(
                                text = content.ifBlank { "No content written yet." },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { subjectExpanded = !subjectExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        val subjName = subjects.firstOrNull { it.id == subjectId }?.name ?: "Education (Major)"
                        OutlinedTextField(
                            value = subjName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Subject") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            subjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj.name) },
                                    onClick = {
                                        subjectId = subj.id
                                        subjectExpanded = false
                                        triggerAutoSave(nextSubject = subj.id)
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (autoSaveStatus == AutoSaveStatus.SAVED) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = "Saved", tint = EmeraldSuccess)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        triggerAutoSave(nextTitle = it)
                    },
                    label = { Text("Note Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("note_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        triggerAutoSave(nextContent = it)
                    },
                    label = { Text("Study Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("note_content_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
}
