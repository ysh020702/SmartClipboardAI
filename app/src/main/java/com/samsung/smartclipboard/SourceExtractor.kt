package com.samsung.smartclipboard

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class SourceExtractor(private val context: Context) {
    private val ocrRecognizer = TextRecognition.getClient(
        KoreanTextRecognizerOptions.Builder().build()
    )

    suspend fun extractFromOcr(uriString: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val uri = Uri.parse(uriString)
            val image = InputImage.fromFilePath(context, uri)
            val result = Tasks.await(ocrRecognizer.process(image))
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun extractFromUrl(url: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(8000)
                .get()
            val mainContent = document.select("article, main, .content, #content, p")
            if (mainContent.isNotEmpty()) mainContent.text() else document.body().text()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}