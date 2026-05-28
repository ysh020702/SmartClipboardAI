package com.samsung.smartclipboard.domain.ai

import com.samsung.smartclipboard.domain.model.AgentResult
import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.Topic

interface TopicAgent {
    /**
     * Topic과 연결된 DataItem들을 분석하여 AgentResult를 반환합니다.
     *
     * @param topic 분석할 Topic
     * @param items Topic에 연결된 DataItem 목록 (빈 리스트 가능 → 실패 Result 반환)
     * @param userInstruction 사용자 추가 지시사항 (nullable, 없으면 null)
     * @return Result<AgentResult> 성공/실패를 Result로 감쌈
     */
    suspend fun analyze(
        topic: Topic,
        items: List<DataItem>,
        userInstruction: String? = null
    ): Result<AgentResult>
}