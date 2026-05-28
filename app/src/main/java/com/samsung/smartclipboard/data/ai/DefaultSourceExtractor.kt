package com.samsung.smartclipboard.data.ai

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.samsung.smartclipboard.domain.ai.SourceExtractor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSourceExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) : SourceExtractor {

    private val ocrRecognizer = TextRecognition.getClient(
        KoreanTextRecognizerOptions.Builder().build()
    )

    override suspend fun extractFromOcr(uriString: String): String = withContext(Dispatchers.IO) {
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

    override suspend fun extractFromUrl(url: String): String = withContext(Dispatchers.IO) {
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