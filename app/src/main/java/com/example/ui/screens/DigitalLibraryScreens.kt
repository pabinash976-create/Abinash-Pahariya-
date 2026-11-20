package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.PdfBookEntity
import com.example.data.PdfPageBookmarkEntity
import com.example.data.SubjectEntity
import com.example.ui.components.BookmarkIconButton
import com.example.ui.components.SubjectBadgePill
import com.example.ui.components.SubjectFilterRow
import com.example.ui.components.WolfCard
import com.example.ui.components.WolfEmptyState
import com.example.ui.components.WolfSearchBar
import com.example.ui.components.WolfTopBar

@Composable
fun DigitalLibraryScreen(
    subjects: List<SubjectEntity>,
    pdfBooks: List<PdfBookEntity>,
    onImportPdf: (Uri, Int, String?) -> Unit,
    onOpenPdfReader: (Long) -> Unit,
    onUpdatePdfMetadata: (PdfBookEntity, String, Int) -> Unit,
    onDeletePdfBook: (PdfBookEntity) -> Unit,
    onTogglePdfBookmark: (PdfBookEntity) -> Unit,
    onOpenAiSearch: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    var pendingUriForSubject by remember { mutableStateOf<Uri?>(null) }
    var editingBook by remember { mutableStateOf<PdfBookEntity?>(null) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingUriForSubject = uri
        }
    }

    val q = searchQuery.trim().lowercase()
    val filteredBooks = pdfBooks.filter { book ->
        val matchesSubject = selectedSubjectId == null || book.subjectId == selectedSubjectId
        val matchesQuery = q.isEmpty() || book.title.lowercase().contains(q) || book.fileName.lowercase().contains(q)
        matchesSubject && matchesQuery
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                modifier = Modifier.testTag("library_import_pdf_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = "Import PDF")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("digital_library_screen")
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "DIGITAL LIBRARY",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                WolfSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search PDF books...",
                    testTag = "library_search_input"
                )
                if (searchQuery.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { onOpenAiSearch(searchQuery.trim()) },
                        modifier = Modifier.fillMaxWidth().testTag("library_ai_search_button")
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

            if (filteredBooks.isEmpty()) {
                WolfEmptyState(
                    icon = Icons.Default.LibraryBooks,
                    title = "Your Digital Library is Empty",
                    subtitle = "Import PDF textbooks, syllabus files, or study guides and organize them by subject.",
                    actionButton = {
                        Button(
                            onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                            modifier = Modifier.testTag("library_empty_import_button")
                        ) {
                            Text("Import PDF File")
                        }
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredBooks, key = { it.id }) { book ->
                        val subject = subjects.firstOrNull { it.id == book.subjectId }
                        WolfCard(
                            onClick = { onOpenPdfReader(book.id) },
                            modifier = Modifier.testTag("library_book_card_${book.id}")
                        ) {
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
                                        contentDescription = "PDF book",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = book.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (subject != null) {
                                                SubjectBadgePill(subject.shortCode, subject.accentColorHex)
                                            }
                                            Text(
                                                text = "${book.pageCount} pages",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Row {
                                    BookmarkIconButton(
                                        isBookmarked = book.isBookmarked,
                                        onClick = { onTogglePdfBookmark(book) }
                                    )
                                    IconButton(
                                        onClick = { editingBook = book },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit))
                                    }
                                    IconButton(
                                        onClick = { onDeletePdfBook(book) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete))
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    val uriToAssign = pendingUriForSubject
    if (uriToAssign != null) {
        PdfImportAssignDialog(
            subjects = subjects,
            initialSubjectId = selectedSubjectId ?: 1,
            onDismiss = { pendingUriForSubject = null },
            onConfirm = { subjectId, title ->
                onImportPdf(uriToAssign, subjectId, title)
                pendingUriForSubject = null
            }
        )
    }

    val bookToEdit = editingBook
    if (bookToEdit != null) {
        PdfEditMetadataDialog(
            book = bookToEdit,
            subjects = subjects,
            onDismiss = { editingBook = null },
            onConfirm = { newTitle, newSubjectId ->
                onUpdatePdfMetadata(bookToEdit, newTitle, newSubjectId)
                editingBook = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfImportAssignDialog(
    subjects: List<SubjectEntity>,
    initialSubjectId: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var chosenSubjectId by remember { mutableIntStateOf(initialSubjectId) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save PDF to Library") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Book Title (optional)") },
                    placeholder = { Text("Auto-filled from PDF filename") }
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    val selectedName = subjects.firstOrNull { it.id == chosenSubjectId }?.name ?: "Education (Major)"
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign to Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        subjects.forEach { subj ->
                            DropdownMenuItem(
                                text = { Text(subj.name) },
                                onClick = {
                                    chosenSubjectId = subj.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(chosenSubjectId, title.takeIf { it.isNotBlank() }) }) {
                Text("Import")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfEditMetadataDialog(
    book: PdfBookEntity,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var title by remember { mutableStateOf(book.title) }
    var chosenSubjectId by remember { mutableIntStateOf(book.subjectId) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit PDF Book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Book Title") })
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    val selectedName = subjects.firstOrNull { it.id == chosenSubjectId }?.name ?: ""
                    OutlinedTextField(
                        value = selectedName,
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
                                    chosenSubjectId = subj.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, chosenSubjectId) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun PdfReaderScreen(
    book: PdfBookEntity?,
    initialPage: Int,
    pageBookmarks: List<PdfPageBookmarkEntity>,
    renderPage: suspend (String, Int, Int) -> Result<Bitmap>,
    onPageChanged: (Long, Int) -> Unit,
    onTogglePageBookmark: (Long, Int, Boolean, String) -> Unit,
    onBack: () -> Unit
) {
    if (book == null) {
        Scaffold(topBar = { WolfTopBar(title = "PDF Reader", onBack = onBack) }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("PDF book not found.")
            }
        }
        return
    }

    var currentPage by remember(book.id, initialPage) {
        mutableIntStateOf(initialPage.coerceIn(0, (book.pageCount - 1).coerceAtLeast(0)))
    }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var renderError by remember { mutableStateOf<String?>(null) }
    var isLoadingPage by remember { mutableStateOf(true) }
    var showBookmarkLabelDialog by remember { mutableStateOf(false) }

    val isPageBookmarked = pageBookmarks.any { it.bookId == book.id && it.pageNumber == currentPage }

    LaunchedEffect(book.id, currentPage) {
        isLoadingPage = true
        renderError = null
        onPageChanged(book.id, currentPage)
        renderPage(book.localPath, currentPage, 1440)
            .onSuccess { bmp ->
                renderedBitmap = bmp
                isLoadingPage = false
            }
            .onFailure { err ->
                renderError = err.localizedMessage ?: "Failed to render page"
                isLoadingPage = false
            }
    }

    Scaffold(
        topBar = {
            WolfTopBar(
                title = book.title,
                subtitle = "Page ${currentPage + 1} of ${book.pageCount}",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            if (isPageBookmarked) {
                                onTogglePageBookmark(book.id, currentPage, true, "")
                            } else {
                                showBookmarkLabelDialog = true
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("pdf_bookmark_page_button")
                    ) {
                        Icon(
                            imageVector = if (isPageBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark PDF page",
                            tint = if (isPageBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (currentPage > 0) currentPage-- },
                            enabled = currentPage > 0,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous page")
                        }
                        Text(
                            text = "${currentPage + 1} / ${book.pageCount}",
                            style = MaterialTheme.typography.labelLarge
                        )
                        IconButton(
                            onClick = { if (currentPage + 1 < book.pageCount) currentPage++ },
                            enabled = currentPage + 1 < book.pageCount,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next page")
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { zoomScale = (zoomScale - 0.25f).coerceIn(1f, 3f) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom out")
                        }
                        Text(
                            text = "${(zoomScale * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium
                        )
                        IconButton(
                            onClick = { zoomScale = (zoomScale + 0.25f).coerceIn(1f, 3f) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom in")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(1f, 3f)
                    }
                }
                .testTag("pdf_reader_screen"),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoadingPage -> CircularProgressIndicator()
                renderError != null -> Text(
                    text = renderError!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp)
                )
                renderedBitmap != null -> Image(
                    bitmap = renderedBitmap!!.asImageBitmap(),
                    contentDescription = "PDF page ${currentPage + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .graphicsLayer(scaleX = zoomScale, scaleY = zoomScale),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    if (showBookmarkLabelDialog) {
        var label by remember { mutableStateOf("Important Page ${currentPage + 1}") }
        AlertDialog(
            onDismissRequest = { showBookmarkLabelDialog = false },
            title = { Text("Bookmark Page ${currentPage + 1}") },
            text = {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Bookmark Note") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    onTogglePageBookmark(book.id, currentPage, false, label)
                    showBookmarkLabelDialog = false
                }) { Text("Save Bookmark") }
            },
            dismissButton = {
                TextButton(onClick = { showBookmarkLabelDialog = false }) { Text("Cancel") }
            }
        )
    }
}
