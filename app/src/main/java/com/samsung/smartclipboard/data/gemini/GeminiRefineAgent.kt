package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.data.agent.FallbackRefineAgent
import com.samsung.smartclipboard.domain.agent.RefineAgent
import com.samsung.smartclipboard.domain.ai.GeminiManager
import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.RetrievalPlan

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
            val prompt = GeminiRefinePrompt.build(topicQuery, plan, selectedItemsForPrompt, currentActionsForPrompt, feedback)
            val rawResponse = geminiManager.run(prompt)

            val parseResult = GeminiActionPlannerJsonParser.parseActions(rawResponse, selectedItemsForPrompt)
            parseResult.fold(
                onSuccess = { actions ->
                    if (validateActions(actions, selectedItemsForPrompt)) {
                        Result.success(actions)
                    } else {
                        fallbackAgent.refineActions(topicQuery, plan, selectedItems, currentActions, feedback)
                    }
                },
                onFailure = {
                    fallbackAgent.refineActions(topicQuery, plan, selectedItems, currentActions, feedback)
                }
            )
        } catch (e: Exception) {
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