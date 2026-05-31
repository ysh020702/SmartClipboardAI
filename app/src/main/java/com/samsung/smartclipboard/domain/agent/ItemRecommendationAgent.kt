package com.samsung.smartclipboard.domain.agent

import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.ItemRecommendationResult
import com.samsung.smartclipboard.domain.model.RetrievalPlan

/**
 * Ranked CandidateItem 목록을 받아 추천 결과를 생성하는 인터페이스.
 *
 * LLM은 추천 이유와 선택 제안을 생성할 뿐이고,
 * 실제 DB 검색/정렬/검증은 DataRetriever + CandidateItemRanker가 담당한다.
 */
interface ItemRecommendationAgent {

    /**
     * @param topicQuery 사용자 입력 주제 문자열
     * @param plan 검색 계획
     * @param candidates CandidateItemRanker.rank()로 정렬된 후보 목록
     * @return 추천 결과. candidates가 empty면 empty result를 반환할 수 있다.
     */
    suspend fun recommend(
        topicQuery: String,
        plan: RetrievalPlan,
        candidates: List<CandidateItem>
    ): Result<ItemRecommendationResult>
}