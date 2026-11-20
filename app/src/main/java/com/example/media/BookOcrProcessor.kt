package com.example.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class BookOcrProcessor(private val context: Context) {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun extractTextFromBitmap(bitmap: Bitmap): Result<String> {
        val image = InputImage.fromBitmap(bitmap, 0)
        return runRecognition(image)
    }

    suspend fun extractTextFromUri(uri: Uri): Result<Pair<Bitmap?, String>> = withContext(Dispatchers.IO) {
        try {
            val bitmap = decodeScaledBitmap(uri)
            val inputImage = if (bitmap != null) {
                InputImage.fromBitmap(bitmap, 0)
            } else {
                InputImage.fromFilePath(context, uri)
            }
            val textResult = runRecognition(inputImage)
            textResult.map { extracted -> bitmap to extracted }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun runRecognition(image: InputImage): Result<String> =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val cleaned = visionText.text.trim()
                    if (continuation.isActive) {
                        if (cleaned.isNotEmpty()) {
                            continuation.resume(Result.success(cleaned))
                        } else {
                            continuation.resume(
                                Result.failure(IllegalStateException("No readable text detected on this page. Try a clearer angle or brighter lighting."))
                            )
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(exception))
                    }
                }
        }

    private fun decodeScaledBitmap(uri: Uri): Bitmap? {
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            var sampleSize = 1
            val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
            while (maxDim / sampleSize > 1600) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOpts)
            }
        }.getOrNull()
    }
}
