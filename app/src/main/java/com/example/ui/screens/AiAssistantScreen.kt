package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiSearchResult
import com.example.ai.AiSearchScope
import com.example.ai.AiStudyMode
import com.example.data.LocalVaultMatch
import com.example.data.StudyLanguage
import com.example.data.SubjectEntity
import com.example.ui.AiAssistantViewModel
import com.example.ui.AiUiState
import com.example.ui.components.SubjectBadgePill
import com.example.ui.components.WolfCard
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldSuccess

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiAssistantScreen(
    aiViewModel: AiAssistantViewModel,
    subjects: List<SubjectEntity>,
    preferredLanguage: StudyLanguage,
    onOpenNote: (Long) -> Unit = {},
    onOpenPdf: (Long) -> Unit = {},
    onOpenSubject: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val messages by aiViewModel.messages.collectAsStateWithLifecycle()
    val uiState by aiViewModel.uiState.collectAsStateWithLifecycle()
    val selectedLanguage by aiViewModel.selectedLanguage.collectAsStateWithLifecycle()
    val selectedMode by aiViewModel.selectedMode.collectAsStateWithLifecycle()
    val selectedSubjectId by aiViewModel.selectedSubjectId.collectAsStateWithLifecycle()
    val attachedBitmap by aiViewModel.attachedBitmap.collectAsStateWithLifecycle()
    val actionBanner by aiViewModel.actionBanner.collectAsStateWithLifecycle()

    val searchQuery by aiViewModel.searchQuery.collectAsStateWithLifecycle()
    val searchScope by aiViewModel.searchScope.collectAsStateWithLifecycle()
    val searchAllSubjects by aiViewModel.searchAllSubjects.collectAsStateWithLifecycle()
    val vaultMatches by aiViewModel.vaultMatches.collectAsStateWithLifecycle()
    val searchResult by aiViewModel.searchResult.collectAsStateWithLifecycle()
    val isSearching by aiViewModel.isSearching.collectAsStateWithLifecycle()
    val searchError by aiViewModel.searchError.collectAsStateWithLifecycle()

    var inputPrompt by remember { mutableStateOf("") }
    var subjectMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(preferredLanguage) {
        if (messages.isEmpty() && searchResult == null) {
            aiViewModel.setLanguage(preferredLanguage)
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, bounds)
                }
                var sampleSize = 1
                val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
                while (maxDim / sampleSize > 1600) {
                    sampleSize *= 2
                }
                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                }
            }.getOrNull()?.let { bmp ->
                aiViewModel.setAttachedBitmap(bmp)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_assistant_screen")
    ) {
        // Header + Language + Subject Context
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (selectedMode == AiStudyMode.AI_SEARCH) Icons.Default.ManageSearch else Icons.Default.AutoAwesome,
                        contentDescription = "AI Tutor",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (selectedMode == AiStudyMode.AI_SEARCH) "WOLF AI SEARCH" else "AI STUDY ASSISTANT",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Assamese • Hindi • English Tutor & Smart Search",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (selectedMode == AiStudyMode.AI_SEARCH && (searchResult != null || vaultMatches.isNotEmpty() || searchQuery.isNotBlank())) {
                    IconButton(
                        onClick = { aiViewModel.clearSearch() },
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("ai_search_clear_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear AI search")
                    }
                } else if (messages.isNotEmpty()) {
                    IconButton(
                        onClick = { aiViewModel.clearChat() },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear chat")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Language Chips (English / Assamese / Hindi)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudyLanguage.entries.forEach { lang ->
                    FilterChip(
                        selected = selectedLanguage == lang,
                        onClick = { aiViewModel.setLanguage(lang) },
                        label = { Text("${lang.displayName} (${lang.nativeLabel})") },
                        modifier = Modifier.testTag("ai_lang_${lang.name.lowercase()}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subject Picker
            ExposedDropdownMenuBox(
                expanded = subjectMenuExpanded,
                onExpandedChange = { subjectMenuExpanded = !subjectMenuExpanded }
            ) {
                val subjName = if (selectedMode == AiStudyMode.AI_SEARCH && searchAllSubjects) {
                    "All 7 Study Subjects"
                } else {
                    subjects.firstOrNull { it.id == selectedSubjectId }?.name ?: "Education (Major)"
                }
                OutlinedTextField(
                    value = subjName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Study Subject Context") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = subjectMenuExpanded,
                    onDismissRequest = { subjectMenuExpanded = false }
                ) {
                    if (selectedMode == AiStudyMode.AI_SEARCH) {
                        DropdownMenuItem(
                            text = { Text("All 7 Study Subjects") },
                            onClick = {
                                aiViewModel.setSearchAllSubjects(true)
                                subjectMenuExpanded = false
                            }
                        )
                    }
                    subjects.forEach { subj ->
                        DropdownMenuItem(
                            text = { Text(subj.name) },
                            onClick = {
                                aiViewModel.setSearchAllSubjects(false)
                                aiViewModel.setSubjectId(subj.id)
                                subjectMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Mode selector chips (AI Search + Tutor modes)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AiStudyMode.entries) { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { aiViewModel.setMode(mode) },
                    leadingIcon = if (mode == AiStudyMode.AI_SEARCH) {
                        {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    label = { Text(mode.label) },
                    modifier = Modifier.testTag("ai_mode_${mode.name.lowercase()}")
                )
            }
        }

        if (actionBanner != null) {
            Surface(
                color = EmeraldSuccess.copy(alpha = 0.16f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(actionBanner!!, style = MaterialTheme.typography.bodySmall, color = EmeraldSuccess, modifier = Modifier.weight(1f))
                    TextButton(onClick = { aiViewModel.clearBanner() }) { Text("OK") }
                }
            }
        }

        if (selectedMode == AiStudyMode.AI_SEARCH) {
            AiSearchModeContent(
                modifier = Modifier.weight(1f),
                searchQuery = searchQuery,
                searchScope = searchScope,
                subjects = subjects,
                selectedSubjectId = selectedSubjectId,
                selectedLanguage = selectedLanguage,
                attachedBitmap = attachedBitmap,
                vaultMatches = vaultMatches,
                searchResult = searchResult,
                isSearching = isSearching,
                searchError = searchError,
                onQueryChange = aiViewModel::updateSearchQuery,
                onScopeChange = aiViewModel::setSearchScope,
                onAttachPhoto = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onRemovePhoto = { aiViewModel.setAttachedBitmap(null) },
                onSubmitSearch = { aiViewModel.runAiSearch(it) },
                onSaveAnswerToNotes = { result ->
                    val formatted = buildString {
                        appendLine(result.directAnswer)
                        if (result.keyPoints.isNotEmpty()) {
                            appendLine()
                            appendLine("Key Points:")
                            result.keyPoints.forEach { appendLine("• $it") }
                        }
                        if (result.examTip.isNotBlank()) {
                            appendLine()
                            appendLine("Exam Tip: ${result.examTip}")
                        }
                    }
                    aiViewModel.saveAiResponseAsNote(
                        title = "AI Search: ${result.query.take(60)}",
                        content = formatted,
                        subjectId = selectedSubjectId
                    )
                },
                onOpenVaultMatch = { match ->
                    when (match.kind) {
                        "NOTE" -> onOpenNote(match.itemId)
                        "PDF" -> onOpenPdf(match.itemId)
                        else -> onOpenSubject(match.subjectId)
                    }
                }
            )
        } else {
            // Conversation / Output Area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty() && uiState is AiUiState.Idle) {
                    item {
                        WolfCard {
                            Text(
                                text = "WOLF STUDY AI TUTOR",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ask me to explain tough topics, summarize chapters, help with assignments, or generate exam-ready Important Questions & MCQs in English, Assamese, or Hindi.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    WolfCard(
                        containerColor = if (msg.isUser) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = if (msg.isUser) "YOU • ${msg.modeLabel} (${msg.languageLabel})" else "WOLF AI • ${msg.languageLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (!msg.isUser) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        aiViewModel.saveAiResponseAsNote(
                                            title = "AI: ${msg.modeLabel}",
                                            content = msg.text,
                                            subjectId = msg.subjectId
                                        )
                                    }
                                ) {
                                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Save to Notes")
                                }
                            }
                        }
                    }
                }

                when (val st = uiState) {
                    is AiUiState.Loading -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                Text("Thinking in ${selectedLanguage.displayName}...", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    is AiUiState.Streaming -> {
                        item {
                            WolfCard(borderColor = MaterialTheme.colorScheme.primary) {
                                Text(
                                    text = "WOLF AI • STREAMING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = st.partialText, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                    is AiUiState.Error -> {
                        item {
                            WolfCard(borderColor = CoralError) {
                                Text(
                                    text = "AI Assistant Error",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CoralError
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = st.message, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    else -> {}
                }
            }

            // Attached Image Preview + Structured Direct-Save Buttons + Input Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                if (attachedBitmap != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Image(
                            bitmap = attachedBitmap!!.asImageBitmap(),
                            contentDescription = "Attached study page",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Page image attached", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        IconButton(onClick = { aiViewModel.setAttachedBitmap(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove image")
                        }
                    }
                }

                if (selectedMode == AiStudyMode.GENERATE_QUESTIONS || selectedMode == AiStudyMode.GENERATE_QUIZ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedMode == AiStudyMode.GENERATE_QUESTIONS) {
                            Button(
                                onClick = {
                                    if (inputPrompt.isNotBlank()) {
                                        aiViewModel.generateAndSaveImportantQuestions(inputPrompt)
                                        inputPrompt = ""
                                    }
                                },
                                enabled = uiState !is AiUiState.Loading && uiState !is AiUiState.Streaming && inputPrompt.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().testTag("ai_import_questions_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Generate & Save 4 Important Qs to Subject")
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (inputPrompt.isNotBlank()) {
                                        aiViewModel.generateAndSaveQuiz(inputPrompt)
                                        inputPrompt = ""
                                    }
                                },
                                enabled = uiState !is AiUiState.Loading && uiState !is AiUiState.Streaming && inputPrompt.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().testTag("ai_import_quiz_button")
                            ) {
                                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Generate & Save 4-Question Quiz to Subject")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("ai_attach_image_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Attach study photo"
                        )
                    }

                    OutlinedTextField(
                        value = inputPrompt,
                        onValueChange = { inputPrompt = it },
                        placeholder = { Text(selectedMode.hint, maxLines = 1) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_prompt_input"),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            val toSend = inputPrompt
                            inputPrompt = ""
                            aiViewModel.sendPrompt(toSend)
                        },
                        enabled = (inputPrompt.isNotBlank() || attachedBitmap != null) &&
                            uiState !is AiUiState.Loading && uiState !is AiUiState.Streaming,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("ai_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send prompt",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiSearchModeContent(
    modifier: Modifier = Modifier,
    searchQuery: String,
    searchScope: AiSearchScope,
    subjects: List<SubjectEntity>,
    selectedSubjectId: Int,
    selectedLanguage: StudyLanguage,
    attachedBitmap: android.graphics.Bitmap?,
    vaultMatches: List<LocalVaultMatch>,
    searchResult: AiSearchResult?,
    isSearching: Boolean,
    searchError: String?,
    onQueryChange: (String) -> Unit,
    onScopeChange: (AiSearchScope) -> Unit,
    onAttachPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onSubmitSearch: (String) -> Unit,
    onSaveAnswerToNotes: (AiSearchResult) -> Unit,
    onOpenVaultMatch: (LocalVaultMatch) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_search_mode_container")
    ) {
        // Dedicated Search Input + Scope Toggle
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search concepts, notes, exam Qs...", maxLines = 1) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        Row {
                            IconButton(
                                onClick = onAttachPhoto,
                                modifier = Modifier.minimumInteractiveComponentSize().testTag("ai_search_attach_photo")
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Search by page photo")
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onQueryChange("") },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search text")
                                }
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSubmitSearch(searchQuery) }),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_search_input")
                )

                Button(
                    onClick = { onSubmitSearch(searchQuery) },
                    enabled = (searchQuery.isNotBlank() || attachedBitmap != null) && !isSearching,
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("ai_search_submit_button")
                ) {
                    Text("Search")
                }
            }

            if (attachedBitmap != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Image(
                        bitmap = attachedBitmap.asImageBitmap(),
                        contentDescription = "Search image",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Visual search photo attached", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    IconButton(onClick = onRemovePhoto) {
                        Icon(Icons.Default.Close, contentDescription = "Remove search photo")
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AiSearchScope.entries.forEach { scope ->
                    FilterChip(
                        selected = searchScope == scope,
                        onClick = { onScopeChange(scope) },
                        label = { Text(scope.label) },
                        modifier = Modifier.testTag("ai_search_scope_${scope.name.lowercase()}")
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isSearching) {
                item {
                    WolfCard(borderColor = MaterialTheme.colorScheme.primary) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            Column {
                                Text(
                                    text = "Searching Study Vault & Gemini AI...",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Synthesizing answer in ${selectedLanguage.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (searchError != null) {
                item {
                    WolfCard(borderColor = CoralError) {
                        Text(
                            text = "AI Search Error",
                            style = MaterialTheme.typography.labelMedium,
                            color = CoralError
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = searchError, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (searchResult != null) {
                item {
                    WolfCard(borderColor = MaterialTheme.colorScheme.primary) {
                        Text(
                            text = "WOLF AI SEARCH RESULT • ${selectedLanguage.displayName.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = searchResult.query,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = searchResult.directAnswer,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (searchResult.keyPoints.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "KEY TAKEAWAYS",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            searchResult.keyPoints.forEach { point ->
                                Text(
                                    text = "• $point",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }

                        if (searchResult.examTip.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Exam Tip: ${searchResult.examTip}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        val subjName = subjects.firstOrNull { it.id == selectedSubjectId }?.name ?: "Subject"
                        OutlinedButton(
                            onClick = { onSaveAnswerToNotes(searchResult) },
                            modifier = Modifier.testTag("ai_search_save_note_button")
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save to $subjName Notes")
                        }

                        if (searchResult.relatedQueries.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "RELATED SEARCHES",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                searchResult.relatedQueries.forEach { related ->
                                    AssistChip(
                                        onClick = { onSubmitSearch(related) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
                                        label = { Text(related, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (vaultMatches.isNotEmpty()) {
                item {
                    Text(
                        text = "MATCHING STUDY VAULT (${vaultMatches.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(vaultMatches, key = { "${it.kind}_${it.itemId}" }) { match ->
                    val subj = subjects.firstOrNull { it.id == match.subjectId }
                    val icon = when (match.kind) {
                        "NOTE" -> Icons.Default.EditNote
                        "PDF" -> Icons.Default.PictureAsPdf
                        "QUESTION" -> Icons.Default.HelpOutline
                        else -> Icons.Default.TaskAlt
                    }
                    WolfCard(
                        onClick = { onOpenVaultMatch(match) },
                        modifier = Modifier.testTag("ai_search_vault_${match.kind.lowercase()}_${match.itemId}")
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
                                Icon(icon, contentDescription = match.kind, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = match.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (subj != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                SubjectBadgePill(subj.shortCode, subj.accentColorHex)
                            }
                        }
                        if (match.snippet.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = match.snippet,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (!isSearching && searchResult == null && vaultMatches.isEmpty() && searchError == null) {
                item {
                    WolfCard {
                        Text(
                            text = "SMART AI SEARCH ACROSS YOUR STUDIES",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Search any academic concept, syllabus question, or scan your own Notes, PDFs, Questions & Tasks together with Gemini AI in English, Assamese, or Hindi.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val sampleSearches = listOf(
                            "Educational Psychology Constructivism",
                            "Assamese Sankardev Borgeet",
                            "Latest Govt Education Schemes"
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            sampleSearches.forEach { sample ->
                                AssistChip(
                                    onClick = { onSubmitSearch(sample) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    label = { Text(sample) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
