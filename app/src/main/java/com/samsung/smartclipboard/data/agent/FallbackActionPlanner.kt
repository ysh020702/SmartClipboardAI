package com.samsung.smartclipboard.data.agent

import com.samsung.smartclipboard.domain.agent.ActionPlanner
import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.RetrievalPlan
import com.samsung.smartclipboard.domain.model.TopicActionType

/**
 * LLM 없이 선택된 CandidateItem 목록만으로 ActionDraft 후보를 만드는 fallback 구현체.
 */
class FallbackActionPlanner : ActionPlanner {

    override suspend fun planActions(
        topicQuery: String,
        plan: RetrievalPlan,
        selectedItems: List<CandidateItem>
    ): Result<List<AgentActionDraft>> {
        if (topicQuery.isBlank()) {
            return Result.failure(IllegalArgumentException("주제가 비어 있습니다"))
        }
        if (selectedItems.isEmpty()) {
            return Result.failure(IllegalArgumentException("선택된 아이템이 없습니다"))
        }

        val itemIds = selectedItems.map { it.item.id }
        val combinedText = selectedItems
            .sortedByDescending { it.relevanceScore }
            .flatMap { c ->
                listOfNotNull(
                    c.item.title,
                    c.item.content.take(200),
                    c.item.source
                )
            }
            .distinct()
            .joinToString("\n")

        val actions = mutableListOf<AgentActionDraft>()

        // SUMMARY - 항상 생성
        val summaryBody = if (combinedText.length > 300) {
            combinedText.take(300) + "..."
        } else {
            combinedText.ifBlank { "선택된 ${selectedItems.size}개 데이터를 기반으로 요약 초안을 생성합니다." }
        }
        actions.add(
            AgentActionDraft(
                type = TopicActionType.SUMMARY,
                confidence = 0.80f,
                reason = "선택된 ${selectedItems.size}개 아이템의 주요 내용을 요약합니다.",
                title = "${topicQuery} 요약",
                body = summaryBody,
                payload = null,
                sourceItemIds = itemIds
            )
        )

        // SHARE_DRAFT
        val shareBody = buildShareBody(selectedItems, topicQuery)
        actions.add(
            AgentActionDraft(
                type = TopicActionType.SHARE_DRAFT,
                confidence = 0.70f,
                reason = "선택된 아이템을 공유 가능한 초안으로 정리합니다.",
                title = "${topicQuery} 공유 초안",
                body = shareBody,
                payload = null,
                sourceItemIds = itemIds
            )
        )

        // TODO
        val todoBody = selectedItems.joinToString("\n") { c ->
            "- ${c.item.title ?: c.item.content.take(80)}"
        }.take(400)
        actions.add(
            AgentActionDraft(
                type = TopicActionType.TODO,
                confidence = 0.75f,
                reason = "선택된 아이템을 할 일 목록으로 변환할 수 있습니다.",
                title = "${topicQuery} 할 일",
                body = todoBody,
                payload = null,
                sourceItemIds = itemIds
            )
        )

        // CALENDAR: selectedItems에 날짜 단서가 있으면 생성
        val dateKeywords = listOf(
            "오전", "오후", "시", "분", "요일",
            "월", "일", "날짜", "일정", "약속",
            "회의", "미팅", "마감", "due", "schedule"
        )
        val hasDateClue = selectedItems.any { c ->
            val text = (c.item.title ?: "") + c.item.content
            dateKeywords.any { text.contains(it) }
        }
        if (hasDateClue) {
            actions.add(
                AgentActionDraft(
                    type = TopicActionType.CALENDAR,
                    confidence = 0.65f,
                    reason = "선택된 데이터에 날짜/시간 단서가 있습니다.",
                    title = "${topicQuery} 일정 등록",
                    body = selectedItems.joinToString("\n") { c ->
                        "- ${c.item.title ?: c.item.content.take(120)}"
                    },
                    payload = null,
                    sourceItemIds = itemIds
                )
            )
        }

        // REMINDER: TODO나 CALENDAR가 있으면 알림으로 추가
        if (actions.size < 4) {
            actions.add(
                AgentActionDraft(
                    type = TopicActionType.REMINDER,
                    confidence = 0.60f,
                    reason = "선택된 아이템을 기반으로 리마인더를 생성할 수 있습니다.",
                    title = "${topicQuery} 리마인더",
                    body = "${selectedItems.size}개 아이템에 대한 알림 초안입니다.",
                    payload = null,
                    sourceItemIds = itemIds.take(3)
                )
            )
        }

        return Result.success(
            actions
                .distinctBy { it.type to it.title }
                .take(5)
        )
    }

    private fun buildShareBody(selectedItems: List<CandidateItem>, topicQuery: String): String {
        val sb = StringBuilder()
        sb.appendLine("주제: $topicQuery")
        sb.appendLine()
        selectedItems.take(5).forEach { candidate ->
            val item = candidate.item
            val label = item.title ?: item.content.take(100)
            sb.appendLine("- $label")
            item.source?.let { sb.appendLine("  출처: $it") }
        }
        return sb.toString().take(500)
    }
}