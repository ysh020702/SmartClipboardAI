package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.DataItemType
import com.samsung.smartclipboard.domain.model.RetrievalPlan
import com.samsung.smartclipboard.domain.model.TopicActionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object GeminiRefinePrompt {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun build(
        topicQuery: String,
        plan: RetrievalPlan,
        selectedItems: List<CandidateItem>,
        currentActions: List<AgentActionDraft>,
        feedback: String
    ): String {
        val itemsJson = buildSelectedItemsJson(selectedItems.take(10))
        val actionsJson = buildCurrentActionsJson(currentActions.take(5))
        val typeNames = TopicActionType.entries.joinToString(", ") { it.name }
        val idList = selectedItems.map { it.item.id }.joinToString(", ")

        return buildString {
            append("너는 Android 앱의 작업 계획을 보완하는 비서다.\n")
            append("사용자가 이미 선택한 DataItem과 현재 ActionDraft 후보를 보고,\n")
            append("사용자 피드백에 맞게 작업 후보를 보완하거나 재정렬해라.\n\n")

            append("## 반드시 지킬 규칙\n")
            append("- 응답은 반드시 JSON object 하나만 출력한다.\n")
            append("- markdown 코드 펜스, 설명문, 주석을 절대 포함하지 마라.\n")
            append("- 새 DataItem을 만들지 마라.\n")
            append("- 아래 sourceItemId 목록 외의 id를 절대 반환하지 마라.\n")
            append("- Android Intent나 도구 실행을 직접 제안하지 말고 앱 내부 ActionDraft 후보만 생성해라.\n")
            append("- title, body, reason은 한국어로 작성해라. (단, 번역 요청은 제외)\n")
            append("- body는 사용자가 편집 가능한 초안 수준으로 작성해라.\n")
            append("- 기존 action을 완전히 버리기보다 피드백을 반영해 보완/재정렬해라.\n")
            append("- 개인정보, URL, 주소, 연락처 등 민감한 값을 불필요하게 그대로 재출력하지 마라.\n\n")
            append("## 퀵 액션 피드백 처리 규칙\n")
            append("- \"더 간결하게\" / \"짧게\": body를 절반 이하로 줄이고, 불필요한 설명·수식어를 제거하라. 핵심 문장만 남겨라.\n")
            append("- \"핵심만 요약\": body를 3~5개의 핵심 포인트로만 재구성하라. 배경 설명은 빼라.\n")
            append("- \"제목 바꿔줘\": title을 더 직관적이고 이해하기 쉬운 표현으로 바꿔라. body는 그대로 유지하라.\n")
            append("- \"영어로 번역\": title과 body를 모두 자연스러운 영어로 번역하라. reason은 한국어로 유지하라.\n\n")

            append("## 사용 가능한 sourceItemId 목록\n$idList\n\n")
            append("## 사용 가능한 action type\n$typeNames\n\n")

            append("## 출력 JSON schema\n")
            append("{\n")
            append("  \"actions\": [\n")
            append("    {\n")
            append("      \"type\": \"SUMMARY\",\n")
            append("      \"confidence\": 0.86,\n")
            append("      \"reason\": \"사용자 피드백에 따라 더 짧은 요약 중심으로 보완했습니다.\",\n")
            append("      \"title\": \"선택한 자료 짧게 요약\",\n")
            append("      \"body\": \"선택된 자료의 핵심만 간단히 정리합니다.\",\n")
            append("      \"payload\": {},\n")
            append("      \"sourceItemIds\": [1, 2, 3]\n")
            append("    }\n")
            append("  ]\n")
            append("}\n\n")

            append("## 필드 규칙\n")
            append("- actions: 1~5개\n- type: 위 type 목록 중 하나만 사용\n- confidence: 0.0~1.0\n")
            append("- reason: 한국어 1문장\n- title: 한국어 짧은 제목\n- body: 편집 가능한 초안\n")
            append("- payload: JSON object, 없으면 {}\n- sourceItemIds: 위 목록에 있는 id만 사용\n\n")

            append("## 사용자 주제: $topicQuery\n\n")
            append("## 검색 키워드: ${plan.keywords.joinToString(", ").ifBlank { "없음" }}\n\n")
            append("## 사용자 피드백\n${feedback.take(1000)}\n\n")
            append("## 선택된 아이템 (${selectedItems.take(10).size}개)\n$itemsJson\n")
            append("## 현재 작업 후보\n$actionsJson")
        }
    }

    private fun buildSelectedItemsJson(items: List<CandidateItem>): String {
        val sb = StringBuilder("[\n")
        items.forEachIndexed { i, c ->
            val item = c.item; val com = if (i < items.size - 1) "," else ""
            sb.append("  {\"id\":${item.id},\"type\":\"${item.type.name}\",")
            sb.append("\"title\":${item.title?.let { "\"${esc(it)}\"" } ?: "null"},")
            sb.append("\"source\":${item.source?.let { "\"${esc(it)}\"" } ?: "null"},")
            sb.append("\"contentPreview\":\"${esc(contentPreview(item))}\",")
            sb.append("\"createdAt\":\"${fmt(item.createdAt)}\",")
            sb.append("\"relevanceScore\":${c.relevanceScore},\"relevanceReason\":\"${esc(c.relevanceReason)}\"")
            sb.append("}$com\n")
        }
        sb.append("]"); return sb.toString()
    }

    private fun buildCurrentActionsJson(actions: List<AgentActionDraft>): String {
        val sb = StringBuilder("[\n")
        actions.forEachIndexed { i, a ->
            val com = if (i < actions.size - 1) "," else ""
            sb.append("  {\"type\":\"${a.type.name}\",\"confidence\":${a.confidence},\"reason\":\"${esc(a.reason)}\",")
            sb.append("\"title\":\"${esc(a.title)}\",\"body\":\"${esc(a.body.take(500))}\",\"sourceItemIds\":[${a.sourceItemIds.joinToString()}]")
            sb.append("}$com\n")
        }
        sb.append("]"); return sb.toString()
    }

    private fun contentPreview(item: com.samsung.smartclipboard.domain.model.DataItem): String = when (item.type) {
        DataItemType.IMAGE, DataItemType.SCREENSHOT, DataItemType.FILE ->
            listOfNotNull(item.title, item.source, item.mimeType).joinToString(" / ").take(350)
        else -> item.content.take(350)
    }

    private fun fmt(millis: Long): String = try { dateFormat.format(Date(millis)) } catch (_: Exception) { millis.toString() }
    private fun esc(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
}
