package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.TopicActionType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object GeminiActionPlannerJsonParser {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parseActions(
        raw: String,
        selectedItems: List<CandidateItem>
    ): Result<List<AgentActionDraft>> {
        return try {
            val jsonText = extractJsonObject(raw)
            val obj = json.parseToJsonElement(jsonText).jsonObject
            val selectedIdSet = selectedItems.map { it.item.id }.toSet()

            val actionsArray = obj["actions"]
            if (actionsArray !is JsonArray) return Result.failure(IllegalArgumentException("actions 배열이 없습니다"))

            val drafts = actionsArray
                .mapNotNull { element ->
                    parseActionDraft(element.jsonObject, selectedIdSet)
                }
                .distinctBy { it.type to it.title }
                .take(5)

            if (drafts.isEmpty()) {
                Result.failure(IllegalArgumentException("유효한 action이 하나도 없습니다"))
            } else if (!validateActions(drafts, selectedIdSet)) {
                Result.failure(IllegalArgumentException("action 검증 실패"))
            } else {
                Result.success(drafts)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    private fun parseActionDraft(obj: JsonObject, selectedIdSet: Set<Long>): AgentActionDraft? {
        val type = parseActionType(obj["type"])
            ?: return null

        val confidence = parseConfidence(obj["confidence"])

        val reason = parseText(obj["reason"], "선택된 아이템을 기반으로 생성된 작업 후보입니다.")
        val title = parseText(obj["title"], "${type.name} 작업").take(100)
        val body = parseText(obj["body"], "선택된 데이터를 기반으로 한 초안입니다.")

        // payload: JSON object → String? (기존 AgentActionDraft.payload는 String?)
        val payload: String? = null // payload를 String으로 처리하지 않고 null로 둠

        val sourceItemIds = parseSourceItemIds(obj["sourceItemIds"], selectedIdSet)

        return AgentActionDraft(
            type = type,
            confidence = confidence,
            reason = reason,
            title = title,
            body = body,
            payload = payload,
            sourceItemIds = sourceItemIds
        )
    }

    private fun parseActionType(element: kotlinx.serialization.json.JsonElement?): TopicActionType? {
        if (element == null) return null
        val raw = element.jsonPrimitive.content.trim().uppercase()
        return try {
            TopicActionType.valueOf(raw)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun parseConfidence(element: kotlinx.serialization.json.JsonElement?): Float {
        if (element == null) return 0.5f
        val raw = element.jsonPrimitive.content
        return raw.toFloatOrNull()?.coerceIn(0.0f, 1.0f)
            ?: raw.toDoubleOrNull()?.toFloat()?.coerceIn(0.0f, 1.0f)
            ?: 0.5f
    }

    private fun parseText(element: kotlinx.serialization.json.JsonElement?, fallback: String): String {
        if (element == null) return fallback
        val raw = element.jsonPrimitive.content.trim()
        return raw.ifBlank { fallback }
    }

    private fun parseSourceItemIds(
        element: kotlinx.serialization.json.JsonElement?,
        selectedIdSet: Set<Long>
    ): List<Long> {
        if (element !is JsonArray) return selectedIdSet.toList()

        val parsed = element
            .mapNotNull { e ->
                val content = e.jsonPrimitive.content
                content.toLongOrNull() ?: content.toDoubleOrNull()?.toLong()
            }
            .filter { it in selectedIdSet }

        return if (parsed.isEmpty()) selectedIdSet.take(3).toList() else parsed.distinct()
    }

    private fun validateActions(
        actions: List<AgentActionDraft>,
        selectedIdSet: Set<Long>
    ): Boolean {
        if (actions.size !in 1..5) return false

        return actions.all { action ->
            action.confidence in 0.0f..1.0f &&
                action.title.isNotBlank() &&
                action.body.isNotBlank() &&
                action.sourceItemIds.all { it in selectedIdSet } &&
                action.sourceItemIds.isNotEmpty()
        }
    }
}