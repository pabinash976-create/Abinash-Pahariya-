package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.StudyLanguage
import com.example.data.UserPreferences
import com.example.ui.BackupOperationState
import com.example.ui.components.WolfCard
import com.example.ui.components.WolfLogoEmblem
import com.example.ui.components.WolfTopBar
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.WolfThemePreset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 14. BACKUP & RESTORE
@Composable
fun BackupRestoreScreen(
    preferences: UserPreferences,
    backupState: BackupOperationState,
    onExportBackup: (Uri, String) -> Unit,
    onImportBackup: (Uri, String, Boolean) -> Unit,
    onToggleHideProfile: (Boolean) -> Unit,
    onResetBackupState: () -> Unit,
    onBack: () -> Unit
) {
    var passphrase by remember { mutableStateOf("") }
    var replaceOnRestore by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            onExportBackup(uri, passphrase.trim())
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportBackup(uri, passphrase.trim(), replaceOnRestore)
        }
    }

    val lastBackupText = remember(preferences.lastBackupTimestamp) {
        if (preferences.lastBackupTimestamp <= 0L) "No backup created yet"
        else "Last backup: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(preferences.lastBackupTimestamp))}"
    }

    Scaffold(
        topBar = { WolfTopBar(title = "Backup & Restore", subtitle = "Export, Import & Encrypt Data", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("backup_restore_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            WolfCard {
                Text(
                    text = "PROTECT PERSONAL INFORMATION",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Set an optional AES-256 encryption passphrase before exporting or restoring your study backup.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text("Backup Passphrase (optional)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("backup_passphrase_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exclude personal profile from exports",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = preferences.hideProfileInBackup,
                        onCheckedChange = onToggleHideProfile
                    )
                }
            }

            WolfCard {
                Text("BACKUP TO DEVICE / CLOUD DRIVE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(lastBackupText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        onResetBackupState()
                        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                        exportLauncher.launch("wolf_study_backup_$stamp.json")
                    },
                    enabled = backupState !is BackupOperationState.InProgress,
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("backup_export_button")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Export Study Backup (.json)")
                }
            }

            WolfCard {
                Text("RESTORE STUDY MATERIALS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Replace existing data on restore",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = replaceOnRestore,
                        onCheckedChange = { replaceOnRestore = it }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        onResetBackupState()
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    enabled = backupState !is BackupOperationState.InProgress,
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("backup_import_button")
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Import & Restore Backup")
                }
            }

            when (backupState) {
                is BackupOperationState.InProgress -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator()
                        Text("Processing backup archive...")
                    }
                }
                is BackupOperationState.Success -> {
                    WolfCard(borderColor = EmeraldSuccess) {
                        Text(backupState.message, style = MaterialTheme.typography.titleMedium, color = EmeraldSuccess)
                        Spacer(modifier = Modifier.height(6.dp))
                        val s = backupState.summary
                        Text(
                            text = "${s.notesCount} Notes • ${s.booksCount} PDFs • ${s.questionsCount} Imp. Qs • ${s.quizzesCount} Quiz • ${s.assignmentsCount} Assignments • ${s.diaryCount} Diary • ${s.timetableCount} Slots • ${s.tasksCount} Tasks",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                is BackupOperationState.Error -> {
                    WolfCard(borderColor = CoralError) {
                        Text(backupState.message, style = MaterialTheme.typography.bodyMedium, color = CoralError)
                    }
                }
                BackupOperationState.Idle -> {}
            }
        }
    }
}

// 15. SETTINGS
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onUpdateProfile: (String, String) -> Unit,
    onSetLanguage: (StudyLanguage) -> Unit,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetThemePreset: (WolfThemePreset) -> Unit,
    onSetPrivacyPin: (String, Boolean) -> Unit,
    onDisableAppLock: () -> Unit,
    onOpenBackup: () -> Unit,
    onClearAllData: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember(preferences.studentName) { mutableStateOf(preferences.studentName) }
    var track by remember(preferences.academicTrack) { mutableStateOf(preferences.academicTrack) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { WolfTopBar(title = "Settings", subtitle = "Profile, Privacy & Preferences", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("settings_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Personal Profile
            WolfCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("PERSONAL PROFILE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("settings_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = track,
                    onValueChange = { track = it },
                    label = { Text("Course / Semester") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onUpdateProfile(name, track) },
                    modifier = Modifier.align(Alignment.End).testTag("settings_save_profile_button")
                ) { Text("Save Profile") }
            }

            // Language Selection
            WolfCard {
                Text("PREFERRED STUDY & AI LANGUAGE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StudyLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = preferences.preferredLanguage == lang,
                            onClick = { onSetLanguage(lang) },
                            label = { Text("${lang.displayName} (${lang.nativeLabel})") },
                            modifier = Modifier.testTag("settings_lang_${lang.name.lowercase()}")
                        )
                    }
                }
            }

            // Theme & Privacy
            WolfCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("APPEARANCE & PREMIUM COLORS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WolfThemePreset.entries.forEach { preset ->
                        val isSelected = preferences.themePreset == preset
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onSetThemePreset(preset) }
                                .testTag("settings_preset_${preset.name.lowercase()}"),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(preset.previewCanvas)
                                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(preset.previewSurface)
                                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(preset.previewAccent)
                                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(preset.displayName, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            text = preset.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected theme",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Dark Mode", style = MaterialTheme.typography.titleMedium)
                        Text("Toggle between Deep Dark & Light Academic mode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = preferences.darkThemeEnabled,
                        onCheckedChange = onSetDarkTheme,
                        modifier = Modifier.testTag("settings_dark_theme_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("App Privacy PIN Lock", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (preferences.appLockEnabled) "Enabled (4-digit PIN required on launch)" else "Off",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = preferences.appLockEnabled,
                        onCheckedChange = { enable ->
                            if (enable) showPinDialog = true else onDisableAppLock()
                        },
                        modifier = Modifier.testTag("settings_app_lock_switch")
                    )
                }
            }

            // Data Management
            WolfCard {
                Text("DATA MANAGEMENT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onOpenBackup,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Open Backup & Restore Center") }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showClearDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralError),
                    modifier = Modifier.fillMaxWidth().testTag("settings_clear_data_button")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Clear Personal Study Data")
                }
            }

            // App Information
            WolfCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WolfLogoEmblem(size = 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 4.dp))
                            Text("WOLF STUDY v1.0", style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            text = "I'M HUNGRY. GIVE ME MORE.\nSTUDY SMART. STAY HUNGRY.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set 4-Digit Privacy PIN") },
            text = {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4) newPin = it.filter { c -> c.isDigit() } },
                    label = { Text("4-Digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length == 4) {
                            onSetPrivacyPin(newPin, true)
                            showPinDialog = false
                        }
                    },
                    enabled = newPin.length == 4
                ) { Text("Enable PIN") }
            },
            dismissButton = { TextButton(onClick = { showPinDialog = false }) { Text("Cancel") } }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Study Materials?") },
            text = { Text("This deletes all notes, imported PDFs, questions, quizzes, diary entries, timetable slots, and tasks. Your 7 subjects stay intact. Export a backup first if needed.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralError)
                ) { Text("Clear Everything") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } }
        )
    }
}
