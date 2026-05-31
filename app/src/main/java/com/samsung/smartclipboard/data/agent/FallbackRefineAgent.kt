package com.samsung.smartclipboard.data.agent

import com.samsung.smartclipboard.domain.agent.RefineAgent
import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.RetrievalPlan
import com.samsung.smartclipboard.domain.model.TopicActionType

class FallbackRefineAgent : RefineAgent {

    override suspend fun refineActions(
        topicQuery: String,
        plan: RetrievalPlan,
        selectedItems: List<CandidateItem>,
        currentActions: List<AgentActionDraft>,
        feedback: String
    ): Result<List<AgentActionDraft>> {
        if (topicQuery.isBlank()) return Result.failure(IllegalArgumentException("주제가 비어 있습니다"))
        if (selectedItems.isEmpty()) return Result.failure(IllegalArgumentException("선택된 아이템이 없습니다"))
        if (currentActions.isEmpty()) return Result.failure(IllegalArgumentException("기존 작업 후보가 없습니다"))
        if (feedback.isBlank()) return Result.failure(IllegalArgumentException("피드백이 비어 있습니다"))

        val selectedIdSet = selectedItems.map { it.item.id }.toSet()
        val lowerFeedback = feedback.lowercase()

        // sourceItemIds가 selectedIdSet subset인 action만 우선 보완
        val validActions = currentActions.filter { it.sourceItemIds.all { id -> id in selectedIdSet } }
        val baseActions = if (validActions.isNotEmpty()) validActions.take(5) else currentActions.take(5)

        // 피드백 기반 우선순위 정렬
        val sorted = sortByFeedback(baseActions, lowerFeedback)

        // 보완
        val refined = sorted.take(5).map { action ->
            val newBody = adjustBody(action.body, lowerFeedback)
            val newConfidence = ((action.confidence + 0.05f).coerceIn(0.0f, 0.95f) * 100).toInt() / 100f
            val newTitle = if (lowerFeedback.contains("짧게") || lowerFeedback.contains("간단히")) {
                action.title.take(30)
            } else action.title

            val validIds = if (action.sourceItemIds.all { it in selectedIdSet }) {
                action.sourceItemIds
            } else {
                action.sourceItemIds.filter { it in selectedIdSet }.ifEmpty { selectedIdSet.take(3).toList() }
            }

            action.copy(
                confidence = newConfidence,
                reason = "사용자 피드백을 반영해 기존 작업 후보를 보완했습니다.",
                title = newTitle,
                body = newBody,
                sourceItemIds = validIds
            )
        }

        return Result.success(refined)
    }

    private fun sortByFeedback(actions: List<AgentActionDraft>, feedback: String): List<AgentActionDraft> {
        val priority = mutableListOf<AgentActionDraft>()
        val others = actions.toMutableList()

        if (feedback.contains("공유") || feedback.contains("초안")) {
            val share = others.filter { it.type == TopicActionType.SHARE_DRAFT }
            priority.addAll(share); others.removeAll(share)
        }
        if (feedback.contains("할 일") || feedback.contains("todo") || feedback.contains("체크")) {
            val todo = others.filter { it.type == TopicActionType.TODO }
            priority.addAll(todo); others.removeAll(todo)
        }
        if (feedback.contains("일정") || feedback.contains("캘린더") || feedback.contains("약속") || feedback.contains("리마인더")) {
            val cal = others.filter { it.type == TopicActionType.CALENDAR || it.type == TopicActionType.REMINDER }
            priority.addAll(cal); others.removeAll(cal)
        }
        priority.addAll(others)
        return priority
    }

    private fun adjustBody(body: String, feedback: String): String {
        val sb = StringBuilder(body.trim())
        if (feedback.contains("짧게") || feedback.contains("간단히") || feedback.contains("요약")) {
            if (sb.length > 500) sb.setLength(500)
        }
        sb.appendLine()
        sb.appendLine()
        sb.append("(사용자 피드백 반영: ${feedback.take(200)})")
        return sb.toString().take(5000)
    }
}