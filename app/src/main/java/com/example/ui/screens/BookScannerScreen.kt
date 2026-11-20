package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.NoteEntity
import com.example.data.SubjectEntity
import com.example.media.BookOcrProcessor
import com.example.ui.components.WolfCard
import com.example.ui.components.WolfTopBar
import com.example.ui.theme.CoralError
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScannerScreen(
    subjects: List<SubjectEntity>,
    ocrProcessor: BookOcrProcessor,
    onSaveScannedNote: (NoteEntity) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraPermissionRequested by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        cameraPermissionRequested = true
    }

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var extractedText by remember { mutableStateOf("") }
    var noteTitle by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableIntStateOf(1) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var ocrError by remember { mutableStateOf<String?>(null) }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isScanning = true
            ocrError = null
            scope.launch {
                ocrProcessor.extractTextFromUri(uri)
                    .onSuccess { (bmp, text) ->
                        capturedBitmap = bmp
                        extractedText = text
                        if (noteTitle.isBlank()) {
                            noteTitle = text.lineSequence().firstOrNull()?.take(48) ?: "Scanned Book Page"
                        }
                        isScanning = false
                    }
                    .onFailure { err ->
                        ocrError = err.localizedMessage ?: "Failed to extract text from image."
                        isScanning = false
                    }
            }
        }
    }

    Scaffold(
        topBar = {
            WolfTopBar(
                title = "Book Scanner (OCR)",
                subtitle = "Capture Pages & Save as Notes",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("book_scanner_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (capturedBitmap == null) {
                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(MaterialTheme.shapes.large)
                    ) {
                        val previewView = remember { PreviewView(context) }
                        DisposableEffect(lifecycleOwner) {
                            val providerFuture = ProcessCameraProvider.getInstance(context)
                            val executor = ContextCompat.getMainExecutor(context)
                            val listener = Runnable {
                                runCatching {
                                    val cameraProvider = providerFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }
                                    val capture = ImageCapture.Builder()
                                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                        .build()
                                    imageCaptureRef = capture
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        capture
                                    )
                                }
                            }
                            providerFuture.addListener(listener, executor)
                            onDispose {
                                runCatching { providerFuture.get().unbindAll() }
                            }
                        }
                        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val capture = imageCaptureRef ?: return@Button
                                isScanning = true
                                ocrError = null
                                capture.takePicture(
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(image: ImageProxy) {
                                            val bmp = imageProxyToBitmap(image)
                                            image.close()
                                            if (bmp != null) {
                                                capturedBitmap = bmp
                                                scope.launch {
                                                    ocrProcessor.extractTextFromBitmap(bmp)
                                                        .onSuccess { text ->
                                                            extractedText = text
                                                            if (noteTitle.isBlank()) {
                                                                noteTitle = text.lineSequence().firstOrNull()?.take(48) ?: "Scanned Page"
                                                            }
                                                            isScanning = false
                                                        }
                                                        .onFailure { err ->
                                                            ocrError = err.localizedMessage ?: "No text detected."
                                                            isScanning = false
                                                        }
                                                }
                                            } else {
                                                isScanning = false
                                                ocrError = "Unable to decode captured frame."
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            isScanning = false
                                            ocrError = exception.localizedMessage ?: "Camera capture failed."
                                        }
                                    }
                                )
                            },
                            enabled = !isScanning,
                            modifier = Modifier.weight(1f).height(50.dp).testTag("scanner_capture_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Capture Page")
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.height(50.dp).testTag("scanner_gallery_button")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Gallery")
                        }
                    }
                } else {
                    WolfCard {
                        Text(
                            text = "Camera Access for Book Scanning",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (cameraPermissionRequested) {
                                "Camera permission was denied. You can still pick a book page photo from your Gallery below."
                            } else {
                                "Grant camera permission to capture textbook pages live, or import a page photo directly from your Gallery."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                modifier = Modifier.weight(1f).testTag("scanner_enable_camera_button")
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Enable Camera")
                            }
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                modifier = Modifier.weight(1f).testTag("scanner_pick_photo_button")
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Pick Photo")
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Scanned page preview",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = {
                            capturedBitmap = null
                            extractedText = ""
                            ocrError = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Retake / Scan Another")
                    }
                }
            }

            if (isScanning) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Text("Extracting readable text with ML Kit OCR...", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (ocrError != null) {
                WolfCard(borderColor = CoralError) {
                    Text(ocrError!!, color = CoralError, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (extractedText.isNotBlank()) {
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded }
                ) {
                    val subjName = subjects.firstOrNull { it.id == selectedSubjectId }?.name ?: "Education (Major)"
                    OutlinedTextField(
                        value = subjName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Save to Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                        subjects.forEach { subj ->
                            DropdownMenuItem(
                                text = { Text(subj.name) },
                                onClick = {
                                    selectedSubjectId = subj.id
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    label = { Text("Note Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("scanner_note_title_input")
                )

                OutlinedTextField(
                    value = extractedText,
                    onValueChange = { extractedText = it },
                    label = { Text("Extracted OCR Text (Editable)") },
                    minLines = 6,
                    modifier = Modifier.fillMaxWidth().testTag("scanner_extracted_text_input")
                )

                Button(
                    onClick = {
                        onSaveScannedNote(
                            NoteEntity(
                                subjectId = selectedSubjectId,
                                title = noteTitle.trim().ifEmpty { "Scanned Book Page" },
                                content = extractedText.trim(),
                                sourceTag = "OCR"
                            )
                        )
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("scanner_save_note_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Scanned Page as Note")
                }
            }
        }
    }
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
    return runCatching {
        val bitmap = image.toBitmap()
        val rotation = image.imageInfo.rotationDegrees
        if (rotation != 0) {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }.getOrNull()
}
