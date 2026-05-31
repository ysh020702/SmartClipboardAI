package com.samsung.smartclipboard.data.agent

import com.samsung.smartclipboard.domain.agent.ItemRecommendationAgent
import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.ItemRecommendationResult
import com.samsung.smartclipboard.domain.model.RetrievalPlan

/**
 * LLM 없이 CandidateItem 목록만으로 추천 결과를 만드는 Fallback 구현체.
 *
 * Gemini 실패, JSON 파싱 실패, itemId 검증 실패 시 fallback으로 사용한다.
 */
class FallbackItemRecommendationAgent : ItemRecommendationAgent {

    override suspend fun recommend(
        topicQuery: String,
        plan: RetrievalPlan,
        candidates: List<CandidateItem>
    ): Result<ItemRecommendationResult> {
        if (topicQuery.isBlank()) {
            return Result.failure(IllegalArgumentException("주제가 비어 있습니다"))
        }

        if (candidates.isEmpty()) {
            return Result.success(
                ItemRecommendationResult(
                    recommendedItems = emptyList(),
                    selectedItemIds = emptySet(),
                    recommendationReason = "검색된 후보 아이템이 없습니다.",
                    suggestedQueries = plan.keywords.take(5)
                )
            )
        }

        // relevanceScore 내림차순, 동점이면 createdAt 최신 순
        val sorted = candidates
            .sortedWith(
                compareByDescending<CandidateItem> { it.relevanceScore }
                    .thenByDescending { it.item.createdAt }
            )

        val recommendedItems = sorted.take(10)
        val topCount = minOf(3, recommendedItems.size)
        val selectedItemIds = recommendedItems.take(topCount).map { it.item.id }.toSet()

        val hasKeywords = plan.keywords.isNotEmpty()
        val recommendationReason = if (hasKeywords) {
            "주제와 일치하는 키워드와 기존 관련도 점수를 기준으로 추천했습니다."
        } else {
            "최근성과 기존 관련도 점수를 기준으로 추천했습니다."
        }

        return Result.success(
            ItemRecommendationResult(
                recommendedItems = recommendedItems,
                selectedItemIds = selectedItemIds,
                recommendationReason = recommendationReason,
                suggestedQueries = plan.keywords.take(5)
            )
        )
    }
}