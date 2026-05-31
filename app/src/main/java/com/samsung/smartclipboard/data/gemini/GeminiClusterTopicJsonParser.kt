package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.domain.model.DataCluster
import com.samsung.smartclipboard.domain.model.SuggestedTopic
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object GeminiClusterTopicJsonParser {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parseClusterTopics(raw: String, originalClusters: List<DataCluster>): Result<List<DataCluster>> {
        return try {
            val jsonText = extractJsonObject(raw)
            val obj = json.parseToJsonElement(jsonText).jsonObject
            val originalById = originalClusters.associateBy { it.clusterId }

            val arr = obj["clusters"]
            if (arr !is JsonArray) return Result.failure(IllegalArgumentException("clusters 배열이 없습니다"))

            val result = arr
                .mapNotNull { element ->
                    parseCluster(element.jsonObject, originalById)
                }
                .take(10)

            if (result.isEmpty()) Result.failure(IllegalArgumentException("유효한 클러스터가 없습니다"))
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

    private fun parseCluster(obj: JsonObject, originalById: Map<String, DataCluster>): DataCluster? {
        val cidElement = obj["clusterId"] ?: return null
        val cid = cidElement.jsonPrimitive.content.trim()
        val original = originalById[cid] ?: return null

        val label = obj["clusterLabel"]?.jsonPrimitive?.content?.trim()?.takeIf { it.isNotBlank() }
            ?: original.clusterLabel

        val topics = parseTopicCandidates(obj["topicCandidates"], cid)

        return original.copy(
            clusterLabel = label,
            topicCandidates = topics,
            generatedAt = original.generatedAt
        )
    }

    private fun parseTopicCandidates(element: kotlinx.serialization.json.JsonElement?, clusterId: String): List<SuggestedTopic> {
        if (element !is JsonArray) return emptyList()
        return element
            .mapNotNull { parseSuggestedTopic(it.jsonObject, clusterId) }
            .distinctBy { it.suggestedTitle }
            .take(3)
    }

    private fun parseSuggestedTopic(obj: JsonObject, clusterId: String): SuggestedTopic? {
        val title = obj["suggestedTitle"]?.jsonPrimitive?.content?.trim()
        if (title.isNullOrBlank()) return null

        val description = obj["description"]?.jsonPrimitive?.content?.trim()
            ?: "관련 데이터를 바탕으로 정리, 요약, 후속 작업을 만들 수 있습니다."

        val confidence = parseConfidence(obj["confidence"])

        val reason = obj["reason"]?.jsonPrimitive?.content?.trim()
            ?: "비슷한 키워드와 데이터 유형이 함께 묶였습니다."

        return SuggestedTopic(
            suggestedTitle = title,
            description = description,
            confidence = confidence,
            reason = reason,
            relatedClusterId = clusterId
        )
    }

    private fun parseConfidence(element: kotlinx.serialization.json.JsonElement?): Float {
        if (element == null) return 0.5f
        val raw = element.jsonPrimitive.content
        return raw.toFloatOrNull()?.coerceIn(0.0f, 1.0f)
            ?: raw.toDoubleOrNull()?.toFloat()?.coerceIn(0.0f, 1.0f)
            ?: 0.5f
    }
}