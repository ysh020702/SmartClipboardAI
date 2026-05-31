package com.samsung.smartclipboard.domain.agent

import com.samsung.smartclipboard.domain.model.RetrievalPlan

/**
 * 사용자가 입력한 주제 문자열을 RetrievalPlan(검색 계획)으로 변환하는 인터페이스.
 *
 * LLM은 검색 계획을 제안할 뿐이고,
 * 실제 DB 검색/정렬/검증은 DataRetriever + CandidateItemRanker가 담당한다.
 */
interface TopicPlanner {

    /**
     * @param topicQuery 사용자 입력 주제 문자열
     * @return 성공 시 RetrievalPlan, 실패 시 원인을 담은 Result.failure
     */
    suspend fun plan(topicQuery: String): Result<RetrievalPlan>
}