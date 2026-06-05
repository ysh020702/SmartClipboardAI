package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.domain.ai.AnalyzedPurpose
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object GeminiPurposeJsonParser {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parsePurposes(raw: String, validIds: Set<Long>): Result<List<AnalyzedPurpose>> {
        return try {
            val jsonText = extractJsonObject(raw)
            val obj = json.parseToJsonElement(jsonText).jsonObject

            val arr = obj["items"]
            if (arr !is JsonArray) return Result.failure(IllegalArgumentException("items 배열이 없습니다"))

            val result = arr
                .mapNotNull { element -> parseItem(element.jsonObject, validIds) }
                .distinctBy { it.itemId }

            if (result.isEmpty()) Result.failure(IllegalArgumentException("유효한 purpose 분석 결과가 없습니다"))
            else Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractJsonObject(raw: String): String {
        var text = raw.replace(Regex("```json\\s*"), "").replace(Regex("```\\s*"), "")
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start >= 0 && end > start) text = text.substring(start, end + 1)
        return text.trim()
    }

    private fun parseItem(obj: kotlinx.serialization.json.JsonObject, validIds: Set<Long>): AnalyzedPurpose? {
        val idElement = obj["id"] ?: return null
        val id = idElement.jsonPrimitive.content.toLongOrNull() ?: return null
        if (id !in validIds) return null

        val purpose = obj["purpose"]?.jsonPrimitive?.content?.trim()
        if (purpose.isNullOrBlank()) return null

        val purposeKeyword = obj["purposeKeyword"]?.jsonPrimitive?.content?.trim()
            ?: extractKeywordsFromPurpose(purpose)

        return AnalyzedPurpose(
            itemId = id,
            purpose = purpose.take(200),
            purposeKeyword = purposeKeyword.take(200)
        )
    }

    /** purposeKeyword가 없을 경우 purpose에서 간단히 키워드 추출 (fallback) */
    private fun extractKeywordsFromPurpose(purpose: String): String {
        return purpose
            .replace(Regex("[은는이가을를에의과와도에서로하]$"), "")
            .split(Regex("[\\s,·]+"))
            .filter { it.length >= 2 }
            .take(7)
            .joinToString(",")
    }
}