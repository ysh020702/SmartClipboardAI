package com.samsung.smartclipboard.domain.agent

import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.RetrievalPlan

/**
 * 선택된 CandidateItem 목록을 기반으로 ActionDraft 후보를 생성하는 인터페이스.
 *
 * LLM은 작업 생성과 payload 추천을 제공할 뿐이고,
 * 실제 Tool 실행/검증은 ToolRouter + ToolExecutor가 담당한다.
 */
interface ActionPlanner {

    /**
     * @param topicQuery 사용자 입력 주제 문자열
     * @param plan 검색 계획
     * @param selectedItems 사용자가 선택한 CandidateItem 목록
     * @return 1~5개의 AgentActionDraft 목록. sourceItemIds는 selectedItems의 id만 포함해야 함.
     */
    suspend fun planActions(
        topicQuery: String,
        plan: RetrievalPlan,
        selectedItems: List<CandidateItem>
    ): Result<List<AgentActionDraft>>
}