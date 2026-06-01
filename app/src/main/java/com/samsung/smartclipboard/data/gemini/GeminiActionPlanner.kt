package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.data.agent.FallbackActionPlanner
import com.samsung.smartclipboard.domain.agent.ActionPlanner
import com.samsung.smartclipboard.domain.ai.GeminiManager
import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.RetrievalPlan
import com.samsung.smartclipboard.util.AgentTraceLogger

class GeminiActionPlanner(
    private val geminiManager: GeminiManager,
    private val fallbackPlanner: ActionPlanner = FallbackActionPlanner()
) : ActionPlanner {

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

        return try {
            val selectedItemsForPrompt = selectedItems.take(10)
            AgentTraceLogger.event(
                stage = "action_planner",
                message = "start",
                details = mapOf(
                    "topicQuery" to topicQuery,
                    "selectedCount" to selectedItems.size,
                    "promptItemIds" to selectedItemsForPrompt.map { it.item.id }
                )
            )
            val prompt = GeminiActionPlannerPrompt.build(topicQuery, plan, selectedItemsForPrompt)
            AgentTraceLogger.prompt("action_planner", prompt)
            val rawResponse = geminiManager.run(prompt)
            AgentTraceLogger.rawResponse("action_planner", rawResponse)

            val parseResult = GeminiActionPlannerJsonParser.parseActions(rawResponse, selectedItemsForPrompt)

            parseResult.fold(
                onSuccess = { actions ->
                    AgentTraceLogger.parsed(
                        stage = "action_planner",
                        message = "action drafts",
                        details = mapOf(
                            "actionCount" to actions.size,
                            "actions" to actions.map { "${it.type}:${it.title}" },
                            "sourceItemIds" to actions.map { it.sourceItemIds }
                        )
                    )
                    if (validateActions(actions, selectedItemsForPrompt)) {
                        Result.success(actions)
                    } else {
                        AgentTraceLogger.fallback("action_planner", "invalid_parsed_actions")
                        fallbackPlanner.planActions(topicQuery, plan, selectedItems)
                    }
                },
                onFailure = { error ->
                    AgentTraceLogger.fallback("action_planner", "parse_failed", error)
                    fallbackPlanner.planActions(topicQuery, plan, selectedItems)
                }
            )
        } catch (e: Exception) {
            AgentTraceLogger.fallback("action_planner", "gemini_exception", e)
            fallbackPlanner.planActions(topicQuery, plan, selectedItems)
        }
    }

    private fun validateActions(
        actions: List<AgentActionDraft>,
        selectedItems: List<CandidateItem>
    ): Boolean {
        val selectedIdSet = selectedItems.map { it.item.id }.toSet()

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
