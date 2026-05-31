package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.ItemRecommendationResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Gemini의 ItemRecommendation JSON 응답을 수동 파싱하는 유틸.
 *
 * JsonObject 기반으로 복구 가능한 파싱을 수행하며,
 * 실제 candidates에 존재하는 itemId만 통과시킨다.
 */
internal object GeminiItemRecommendationJsonParser {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * @param raw Gemini 원시 응답 문자열
     * @param candidates 원본 CandidateItem 목록 (id 검증 용도)
     * @return 파싱 및 검증된 ItemRecommendationResult
     */
    fun parseItemRecommendation(
        raw: String,
        candidates: List<CandidateItem>
    ): Result<ItemRecommendationResult> {
        return try {
            val jsonText = extractJsonObject(raw)
            val obj = json.parseToJsonElement(jsonText).jsonObject

            val candidateById: Map<Long, CandidateItem> = candidates.associateBy { it.item.id }
            val selectedItemIds = parseSelectedItemIds(obj, candidateById)
            val itemReasonsMap = parseItemReasons(obj, candidateById)

            val recommendedItems = buildRecommendedItems(candidates, itemReasonsMap)
            val finalSelectedIds = if (selectedItemIds.isNotEmpty()) {
                selectedItemIds.filter { id -> id in candidateById }
            } else if (recommendedItems.isNotEmpty()) {
                recommendedItems.take(3).map { it.item.id }.toSet()
            } else {
                emptySet()
            }.toSet()

            // recommendedItems에 포함된 id만 최종 selected로 유지
            val validSelectedIds = finalSelectedIds.filter { id ->
                recommendedItems.any { it.item.id == id }
            }.toSet()

            val recommendationReason = parseRecommendationReason(obj)
            val suggestedQueries = parseSuggestedQueries(obj)

            Result.success(
                ItemRecommendationResult(
                    recommendedItems = recommendedItems,
                    selectedItemIds = validSelectedIds,
                    recommendationReason = recommendationReason,
                    suggestedQueries = suggestedQueries
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- private helpers ---

    private fun extractJsonObject(raw: String): String {
        var text = raw
            .replace(Regex("```json\\s*"), "")
            .replace(Regex("```\\s*"), "")

        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start >= 0 && end > start) {
            text = text.substring(start, end + 1)
        }
        return text.trim()
    }

    private fun parseLongId(element: kotlinx.serialization.json.JsonElement): Long? {
        val content = element.jsonPrimitive.content
        return content.toLongOrNull() ?: content.toDoubleOrNull()?.toLong()
    }

    private fun parseSelectedItemIds(
        obj: JsonObject,
        candidateById: Map<Long, CandidateItem>
    ): Set<Long> {
        val arr = obj["selectedItemIds"] ?: return emptySet()
        if (arr !is JsonArray) return emptySet()

        return arr
            .mapNotNull { element ->
                parseLongId(element)?.takeIf { it in candidateById }
            }
            .distinct()
            .take(5)
            .toSet()
    }

    private class ParsedItemReason(
        val itemId: Long,
        val score: Float,
        val reason: String
    )

    private fun parseItemReasons(
        obj: JsonObject,
        candidateById: Map<Long, CandidateItem>
    ): Map<Long, ParsedItemReason> {
        val arr = obj["itemReasons"] ?: return emptyMap()
        if (arr !is JsonArray) return emptyMap()

        return arr
            .mapNotNull { element ->
                val itemObj = element.jsonObject
                val rawId = itemObj["itemId"] ?: return@mapNotNull null
                val itemId = parseLongId(rawId) ?: return@mapNotNull null
                if (itemId !in candidateById) return@mapNotNull null

                val scoreElement = itemObj["score"]
                val rawScore = scoreElement?.jsonPrimitive?.content?.toFloatOrNull() ?: candidateById[itemId]!!.relevanceScore
                val score = clampScore(rawScore)

                val reasonElement = itemObj["reason"]
                val reason = reasonElement?.jsonPrimitive?.content
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: candidateById[itemId]!!.relevanceReason

                ParsedItemReason(itemId, score, reason)
            }
            .take(10)
            .associateBy { it.itemId }
    }

    private fun buildRecommendedItems(
        candidates: List<CandidateItem>,
        itemReasonsMap: Map<Long, ParsedItemReason>
    ): List<CandidateItem> {
        val candidateById = candidates.associateBy { it.item.id }
        val result = mutableListOf<CandidateItem>()

        // 1. itemReasons에 포함된 아이템 우선
        for ((itemId, parsed) in itemReasonsMap) {
            val original = candidateById[itemId] ?: continue
            val updated = original.copy(
                relevanceScore = parsed.score,
                relevanceReason = parsed.reason
            )
            if (updated !in result) {
                result.add(updated)
            }
        }

        // 2. 부족하면 기존 candidates를 relevanceScore 높은 순으로 채움
        val addedIds = result.map { it.item.id }.toSet()
        val remaining = candidates
            .filter { it.item.id !in addedIds }
            .sortedByDescending { it.relevanceScore }

        for (candidate in remaining) {
            if (result.size >= 10) break
            result.add(candidate)
        }

        return result
    }

    private fun parseRecommendationReason(obj: JsonObject): String {
        val element = obj["recommendationReason"]
        val raw = element?.jsonPrimitive?.content?.trim()
        return if (raw.isNullOrBlank()) {
            "주제와 후보 아이템의 관련도를 기준으로 추천했습니다."
        } else {
            raw
        }
    }

    private fun parseSuggestedQueries(obj: JsonObject): List<String> {
        val arr = obj["suggestedQueries"] ?: return emptyList()
        if (arr !is JsonArray) return emptyList()

        return arr
            .mapNotNull { element ->
                element.jsonPrimitive.content
                    .takeIf { it != "null" }
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
            }
            .distinct()
            .take(5)
    }

    private fun clampScore(score: Float): Float {
        return score.coerceIn(0.0f, 1.0f)
    }
}