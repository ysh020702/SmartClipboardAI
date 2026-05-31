package com.samsung.smartclipboard.domain.agent

import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.RetrievalPlan

/**
 * 사용자 피드백을 기반으로 기존 ActionDraft 후보를 보완하는 인터페이스.
 *
 * LLM은 작업 후보의 타이틀/본문/타입/정렬을 보완할 뿐이고,
 * DB 검색, Tool 실행, UI 변경은 하지 않는다.
 */
interface RefineAgent {

    /**
     * @param topicQuery 원본 주제
     * @param plan 검색 계획
     * @param selectedItems 사용자가 선택한 CandidateItem 목록 (sourceItemIds 검증 용)
     * @param currentActions 기존 ActionDraft 후보 목록
     * @param feedback 사용자 피드백 문자열
     * @return 보완된 ActionDraft 목록 (1~5개). sourceItemIds는 selectedItems의 id subset이어야 함.
     */
    suspend fun refineActions(
        topicQuery: String,
        plan: RetrievalPlan,
        selectedItems: List<CandidateItem>,
        currentActions: List<AgentActionDraft>,
        feedback: String
    ): Result<List<AgentActionDraft>>
}