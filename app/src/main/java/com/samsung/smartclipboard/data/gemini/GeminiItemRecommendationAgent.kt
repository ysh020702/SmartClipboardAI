package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.data.agent.FallbackItemRecommendationAgent
import com.samsung.smartclipboard.domain.agent.ItemRecommendationAgent
import com.samsung.smartclipboard.domain.ai.GeminiManager
import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.ItemRecommendationResult
import com.samsung.smartclipboard.domain.model.RetrievalPlan
import com.samsung.smartclipboard.util.AgentTraceLogger

/**
 * Gemini LLM을 통해 추천 아이템 선택과 이유 생성을 수행하는 ItemRecommendationAgent 구현체.
 *
 * Gemini 호출 실패, JSON 파싱 실패, 결과 검증 실패 시 FallbackItemRecommendationAgent로 fallback한다.
 */
class GeminiItemRecommendationAgent(
    private val geminiManager: GeminiManager,
    private val fallbackAgent: ItemRecommendationAgent = FallbackItemRecommendationAgent()
) : ItemRecommendationAgent {

    override suspend fun recommend(
        topicQuery: String,
        plan: RetrievalPlan,
        candidates: List<CandidateItem>
    ): Result<ItemRecommendationResult> {
        if (topicQuery.isBlank()) {
            return Result.failure(IllegalArgumentException("주제가 비어 있습니다"))
        }

        if (candidates.isEmpty()) {
            AgentTraceLogger.fallback("item_recommendation", "empty_candidates")
            return fallbackAgent.recommend(topicQuery, plan, candidates)
        }

        return try {
            val candidatesForPrompt = candidates.take(20)
            AgentTraceLogger.event(
                stage = "item_recommendation",
                message = "start",
                details = mapOf(
                    "topicQuery" to topicQuery,
                    "candidateCount" to candidates.size,
                    "promptCandidateIds" to candidatesForPrompt.map { it.item.id }
                )
            )
            val prompt = GeminiItemRecommendationPrompt.build(topicQuery, plan, candidatesForPrompt)
            AgentTraceLogger.prompt("item_recommendation", prompt)
            val rawResponse = geminiManager.run(prompt)
            AgentTraceLogger.rawResponse("item_recommendation", rawResponse)

            val parseResult = GeminiItemRecommendationJsonParser.parseItemRecommendation(
                raw = rawResponse,
                candidates = candidatesForPrompt
            )

            parseResult.fold(
                onSuccess = { result ->
                    AgentTraceLogger.parsed(
                        stage = "item_recommendation",
                        message = "recommendation result",
                        details = mapOf(
                            "recommendedIds" to result.recommendedItems.map { it.item.id },
                            "selectedItemIds" to result.selectedItemIds,
                            "suggestedQueries" to result.suggestedQueries
                        )
                    )
                    if (validateResult(result, candidatesForPrompt)) {
                        Result.success(result)
                    } else {
                        AgentTraceLogger.fallback("item_recommendation", "invalid_parsed_result")
                        fallbackAgent.recommend(topicQuery, plan, candidates)
                    }
                },
                onFailure = { error ->
                    AgentTraceLogger.fallback("item_recommendation", "parse_failed", error)
                    fallbackAgent.recommend(topicQuery, plan, candidates)
                }
            )
        } catch (e: Exception) {
            AgentTraceLogger.fallback("item_recommendation", "gemini_exception", e)
            fallbackAgent.recommend(topicQuery, plan, candidates)
        }
    }

    /**
     * 파싱된 ItemRecommendationResult가 유효한지 검증한다.
     */
    private fun validateResult(
        result: ItemRecommendationResult,
        originalCandidates: List<CandidateItem>
    ): Boolean {
        val candidateIds = originalCandidates.map { it.item.id }.toSet()

        // recommendedItems의 모든 item.id는 원본 candidates에 존재해야 한다
        val allRecommendedIdsExist = result.recommendedItems.all { it.item.id in candidateIds }
        if (!allRecommendedIdsExist) return false

        // recommendedItems는 최대 10개
        if (result.recommendedItems.size > 10) return false

        // selectedItemIds의 모든 id는 recommendedItems에 존재해야 한다
        val recommendedIds = result.recommendedItems.map { it.item.id }.toSet()
        val allSelectedInRecommended = result.selectedItemIds.all { it in recommendedIds }
        if (!allSelectedInRecommended) return false

        // selectedItemIds는 최대 5개
        if (result.selectedItemIds.size > 5) return false

        // 모든 relevanceScore는 0.0f..1.0f 범위
        val allScoresValid = result.recommendedItems.all {
            it.relevanceScore in 0.0f..1.0f
        }
        if (!allScoresValid) return false

        // recommendationReason은 blank가 아니어야 한다
        if (result.recommendationReason.isBlank()) return false

        return true
    }
}
