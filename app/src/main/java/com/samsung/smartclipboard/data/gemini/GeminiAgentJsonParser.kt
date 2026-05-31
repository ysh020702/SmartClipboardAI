package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.domain.model.DataItemType
import com.samsung.smartclipboard.domain.model.RetrievalPlan
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 새 에이전트 전용 JSON 파서 유틸.
 *
 * 기존 AgentJsonParser.kt를 수정하지 않고,
 * kotlinx.serialization DTO 대신 JsonObject 기반 수동 파싱으로 RetrievalPlan을 생성한다.
 */
internal object GeminiAgentJsonParser {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Gemini 원시 응답에서 RetrievalPlan을 파싱한다.
     *
     * @param raw GeminiManager.run()에서 반환된 원시 응답 문자열
     * @return 성공 시 RetrievalPlan, 실패 시 원인을 담은 Result.failure
     */
    fun parseRetrievalPlan(raw: String): Result<RetrievalPlan> {
        return try {
            val jsonText = extractJsonObject(raw)
            val obj = json.parseToJsonElement(jsonText).jsonObject

            val keywords = parseKeywords(obj)
            val typeFilters = parseTypeFilters(obj)
            val dateRangeDays = parseDateRangeDays(obj)
            val maxResults = parseMaxResults(obj)

            Result.success(
                RetrievalPlan(
                    keywords = keywords,
                    typeFilters = typeFilters,
                    dateRangeDays = dateRangeDays,
                    maxResults = maxResults
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- private helpers ---

    /**
     * raw 응답에서 JSON object 부분만 추출한다.
     * markdown fence 제거, 앞뒤 설명문 제거.
     */
    private fun extractJsonObject(raw: String): String {
        // 1. markdown code fence 제거 (```json ... ``` 또는 ``` ... ```)
        var text = raw
            .replace(Regex("```json\\s*"), "")
            .replace(Regex("```\\s*"), "")

        // 2. 첫 번째 '{' 부터 마지막 '}' 까지 추출
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start >= 0 && end > start) {
            text = text.substring(start, end + 1)
        }
        return text.trim()
    }

    /**
     * keywords 배열 파싱.
     * - 문자열만 허용, trim, 빈 문자열 제거, distinct, 최대 8개
     * - 배열이 없거나 비어 있으면 emptyList (fallback으로 넘기기 위함)
     */
    private fun parseKeywords(obj: JsonObject): List<String> {
        val arr = obj["keywords"] ?: return emptyList()
        if (arr !is JsonArray) return emptyList()

        return arr
            .mapNotNull { element ->
                val s = element.jsonPrimitive.content.takeIf { it != "null" } ?: return@mapNotNull null
                s.trim().takeIf { it.isNotEmpty() }
            }
            .distinct()
            .take(8)
    }

    /**
     * typeFilters 배열 파싱.
     * - 문자열을 DataItemType.valueOf로 변환
     * - 알 수 없는 값은 무시, distinct
     * - 배열이 없으면 emptyList
     */
    private fun parseTypeFilters(obj: JsonObject): List<DataItemType> {
        val arr = obj["typeFilters"] ?: return emptyList()
        if (arr !is JsonArray) return emptyList()

        return arr
            .mapNotNull { element ->
                val s = element.jsonPrimitive.content.takeIf { it != "null" } ?: return@mapNotNull null
                try {
                    DataItemType.valueOf(s.trim().uppercase())
                } catch (_: IllegalArgumentException) {
                    null // 알 수 없는 값 무시
                }
            }
            .distinct()
    }

    /**
     * dateRangeDays 파싱.
     * - null, 누락, 0 이하 → null
     * - 1..365 범위로 coerce
     */
    private fun parseDateRangeDays(obj: JsonObject): Int? {
        val element = obj["dateRangeDays"] ?: return null
        val content = element.jsonPrimitive.content
        if (content == "null") return null

        val value = try {
            content.toIntOrNull() ?: content.toDoubleOrNull()?.toInt()
        } catch (_: Exception) {
            null
        } ?: return null

        if (value <= 0) return null
        return value.coerceIn(1, 365)
    }

    /**
     * maxResults 파싱.
     * - 누락 → 기본 20
     * - 5..50 범위로 coerce
     */
    private fun parseMaxResults(obj: JsonObject): Int {
        val element = obj["maxResults"] ?: return 20
        val content = element.jsonPrimitive.content
        if (content == "null") return 20

        val value = try {
            content.toIntOrNull() ?: content.toDoubleOrNull()?.toInt()
        } catch (_: Exception) {
            null
        } ?: return 20

        return value.coerceIn(5, 50)
    }
}