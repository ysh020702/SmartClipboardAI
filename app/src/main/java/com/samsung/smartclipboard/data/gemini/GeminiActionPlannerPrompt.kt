package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.DataItemType
import com.samsung.smartclipboard.domain.model.RetrievalPlan
import com.samsung.smartclipboard.domain.model.TopicActionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object GeminiActionPlannerPrompt {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun build(
        topicQuery: String,
        plan: RetrievalPlan,
        selectedItems: List<CandidateItem>
    ): String {
        val limitedItems = selectedItems.take(10)
        val itemsJson = buildItemsJson(limitedItems)
        val idList = selectedItems.map { it.item.id }.joinToString(", ")
        val typeNames = TopicActionType.entries.joinToString(", ") { it.name }

        return buildString {
            append("너는 Android 앱의 작업 계획 비서다.\n")
            append("사용자가 선택한 DataItem 목록을 보고 가능한 작업 후보(ActionDraft)를 생성해라.\n\n")

            append("## 반드시 지킬 규칙\n")
            append("- 응답은 반드시 JSON object 하나만 출력한다.\n")
            append("- markdown 코드 펜스(\\`\\`\\`), 설명문, 주석을 절대 포함하지 마라.\n")
            append("- JSON object 외에 다른 텍스트를 출력하지 마라.\n")
            append("- 새 DataItem을 만들지 마라.\n")
            append("- 아래 sourceItemId 목록 외의 id를 절대 반환하지 마라.\n")
            append("- Android Intent나 도구 실행을 직접 제안하지 말고, 앱 내부 ActionDraft 후보만 생성해라.\n")
            append("- title, body, reason은 한국어로 작성해라.\n")
            append("- body는 사용자가 편집 가능한 초안 수준으로 작성해라.\n")
            append("- 개인정보, URL, 주소, 연락처 등 민감한 값을 불필요하게 그대로 재출력하지 마라.\n\n")

            append("## 사용 가능한 sourceItemId 목록\n")
            append("$idList\n\n")

            append("## 사용 가능한 action type\n")
            append("$typeNames\n\n")

            append("## 출력 JSON schema\n")
            append("{\n")
            append("  \"actions\": [\n")
            append("    {\n")
            append("      \"type\": \"SUMMARY\",\n")
            append("      \"confidence\": 0.86,\n")
            append("      \"reason\": \"선택된 아이템들의 핵심을 요약합니다.\",\n")
            append("      \"title\": \"선택한 자료 요약\",\n")
            append("      \"body\": \"선택된 자료의 핵심 내용을 정리한 요약 초안입니다.\",\n")
            append("      \"payload\": {},\n")
            append("      \"sourceItemIds\": [1, 2, 3]\n")
            append("    }\n")
            append("  ]\n")
            append("}\n\n")

            append("## 필드 규칙\n")
            append("- actions: 1~5개\n")
            append("- type: 위 type 목록 중 하나만 사용\n")
            append("- confidence: 0.0~1.0\n")
            append("- reason: 한국어 1문장\n")
            append("- title: 한국어 짧은 제목\n")
            append("- body: 사용자가 편집 가능한 초안\n")
            append("- payload: JSON object, 없으면 {}\n")
            append("- sourceItemIds: 위 sourceItemId 목록에 있는 id만 사용, 비어 있으면 전체 사용\n\n")

            append("## 사용자 주제\n")
            append("$topicQuery\n\n")

            append("## 검색 계획\n")
            append("키워드: ${plan.keywords.joinToString(", ").ifBlank { "없음" }}\n\n")

            append("## 선택된 아이템 목록 (${limitedItems.size}개)\n")
            append(itemsJson)
        }
    }

    private fun buildItemsJson(items: List<CandidateItem>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        items.forEachIndexed { index, candidate ->
            val item = candidate.item
            val comma = if (index < items.size - 1) "," else ""
            sb.append("  {\n")
            sb.append("    \"id\": ${item.id},\n")
            sb.append("    \"type\": \"${item.type.name}\",\n")
            sb.append("    \"title\": ${item.title?.let { "\"${escapeJson(it)}\"" } ?: "null"},\n")
            sb.append("    \"source\": ${item.source?.let { "\"${escapeJson(it)}\"" } ?: "null"},\n")
            sb.append("    \"contentPreview\": \"${escapeJson(contentPreview(item))}\",\n")
            sb.append("    \"createdAt\": \"${formatDate(item.createdAt)}\",\n")
            sb.append("    \"relevanceScore\": ${candidate.relevanceScore},\n")
            sb.append("    \"relevanceReason\": \"${escapeJson(candidate.relevanceReason)}\"\n")
            sb.append("  }$comma\n")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun contentPreview(item: com.samsung.smartclipboard.domain.model.DataItem): String {
        return when (item.type) {
            DataItemType.FILE -> {
                listOfNotNull(item.title, item.source, item.mimeType)
                    .joinToString(" / ")
                    .take(1000)
            }
            else -> item.effectiveContent.take(1000)
        }
    }

    private fun formatDate(epochMillis: Long): String {
        return try {
            dateFormat.format(Date(epochMillis))
        } catch (_: Exception) {
            epochMillis.toString()
        }
    }

    private fun escapeJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
