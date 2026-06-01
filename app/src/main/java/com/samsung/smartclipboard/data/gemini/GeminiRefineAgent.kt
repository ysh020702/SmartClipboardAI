package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.data.agent.FallbackRefineAgent
import com.samsung.smartclipboard.domain.agent.RefineAgent
import com.samsung.smartclipboard.domain.ai.GeminiManager
import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.RetrievalPlan
import com.samsung.smartclipboard.util.AgentTraceLogger

class GeminiRefineAgent(
    private val geminiManager: GeminiManager,
    private val fallbackAgent: RefineAgent = FallbackRefineAgent()
) : RefineAgent {

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

        return try {
            val selectedItemsForPrompt = selectedItems.take(10)
            val currentActionsForPrompt = currentActions.take(5)
            AgentTraceLogger.event(
                stage = "refine_agent",
                message = "start",
                details = mapOf(
                    "topicQuery" to topicQuery,
                    "feedback" to feedback,
                    "selectedItemIds" to selectedItemsForPrompt.map { it.item.id },
                    "currentActionCount" to currentActionsForPrompt.size
                )
            )
            val prompt = GeminiRefinePrompt.build(topicQuery, plan, selectedItemsForPrompt, currentActionsForPrompt, feedback)
            AgentTraceLogger.prompt("refine_agent", prompt)
            val rawResponse = geminiManager.run(prompt)
            AgentTraceLogger.rawResponse("refine_agent", rawResponse)

            val parseResult = GeminiActionPlannerJsonParser.parseActions(rawResponse, selectedItemsForPrompt)
            parseResult.fold(
                onSuccess = { actions ->
                    AgentTraceLogger.parsed(
                        stage = "refine_agent",
                        message = "refined actions",
                        details = mapOf(
                            "actionCount" to actions.size,
                            "actions" to actions.map { "${it.type}:${it.title}" },
                            "sourceItemIds" to actions.map { it.sourceItemIds }
                        )
                    )
                    if (validateActions(actions, selectedItemsForPrompt)) {
                        Result.success(actions)
                    } else {
                        AgentTraceLogger.fallback("refine_agent", "invalid_parsed_actions")
                        fallbackAgent.refineActions(topicQuery, plan, selectedItems, currentActions, feedback)
                    }
                },
                onFailure = { error ->
                    AgentTraceLogger.fallback("refine_agent", "parse_failed", error)
                    fallbackAgent.refineActions(topicQuery, plan, selectedItems, currentActions, feedback)
                }
            )
        } catch (e: Exception) {
            AgentTraceLogger.fallback("refine_agent", "gemini_exception", e)
            fallbackAgent.refineActions(topicQuery, plan, selectedItems, currentActions, feedback)
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
