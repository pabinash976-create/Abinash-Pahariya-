package com.example.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class ImportedPdfInfo(
    val localPath: String,
    val fileName: String,
    val suggestedTitle: String,
    val pageCount: Int
)

class PdfLibraryManager(private val context: Context) {

    private val renderMutex = Mutex()

    private fun pdfDir(): File {
        val dir = File(context.filesDir, "wolf_study_pdfs")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun importPdf(uri: Uri): Result<ImportedPdfInfo> = withContext(Dispatchers.IO) {
        try {
            val displayName = resolveDisplayName(uri) ?: "Study_Document_${System.currentTimeMillis()}.pdf"
            val sanitized = displayName.replace(Regex("[^A-Za-z0-9._-]"), "_")
            val destFile = File(pdfDir(), "${UUID.randomUUID().toString().take(8)}_$sanitized")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(IllegalStateException("Unable to open selected PDF file."))

            val pageCount = countPages(destFile)
            if (pageCount <= 0) {
                destFile.delete()
                return@withContext Result.failure(IllegalStateException("Selected file is not a valid PDF document."))
            }

            val suggestedTitle = displayName.substringBeforeLast('.').replace('_', ' ').trim()
                .ifEmpty { "Study PDF" }

            Result.success(
                ImportedPdfInfo(
                    localPath = destFile.absolutePath,
                    fileName = displayName,
                    suggestedTitle = suggestedTitle,
                    pageCount = pageCount
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renderPage(localPath: String, pageIndex: Int, targetWidthPx: Int): Result<Bitmap> =
        withContext(Dispatchers.IO) {
            renderMutex.withLock {
                try {
                    val file = File(localPath)
                    if (!file.exists()) {
                        return@withLock Result.failure(IllegalStateException("PDF file is missing on device."))
                    }
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                        PdfRenderer(fd).use { renderer ->
                            if (renderer.pageCount <= 0) {
                                return@withLock Result.failure(IllegalStateException("PDF has no readable pages."))
                            }
                            val safeIndex = pageIndex.coerceIn(0, renderer.pageCount - 1)
                            renderer.openPage(safeIndex).use { page ->
                                val width = targetWidthPx.coerceIn(400, 2000)
                                val scale = width.toFloat() / page.width.coerceAtLeast(1)
                                val height = (page.height * scale).toInt().coerceAtLeast(1)
                                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(bitmap)
                                canvas.drawColor(Color.WHITE)
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                Result.success(bitmap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }

    fun deleteLocalPdf(localPath: String) {
        if (localPath.isNotBlank()) {
            runCatching { File(localPath).delete() }
        }
    }

    private fun countPages(file: File): Int {
        return try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                PdfRenderer(fd).use { renderer ->
                    renderer.pageCount
                }
            }
        } catch (_: Exception) {
            0
        }
    }

    private fun resolveDisplayName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) cursor.getString(idx) else null
                    } else null
                }
        }.getOrNull()
    }
}
