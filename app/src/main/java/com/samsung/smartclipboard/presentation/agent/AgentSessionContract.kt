package com.samsung.smartclipboard.presentation.agent

import com.samsung.smartclipboard.domain.model.AgentSession
import com.samsung.smartclipboard.domain.model.AgentSessionState
import com.samsung.smartclipboard.domain.model.RetrievalPlan

/**
 * AgentSessionScreen과 AgentSessionViewModel 사이의 UI 상태/이벤트 계약.
 */
data class AgentSessionUiState(
    val topicQuery: String = "",
    val session: AgentSession? = null,
    val agentState: AgentSessionState = AgentSessionState.Idle,
    val retrievalPlan: RetrievalPlan? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val refineFeedback: String = ""
)

/**
 * 퀵 보완 액션 — 결과 아래에 버튼으로 표시되는 사전 정의 피드백.
 */
enum class QuickRefineAction(val label: String, val feedback: String) {
    MORE_CONCISE("더 간결하게", "더 간결하고 짧게 만들어줘. 불필요한 설명은 빼고 핵심만 남겨줘."),
    KEY_SUMMARY("핵심만 요약", "핵심 포인트만 요약해줘. 가장 중요한 내용만 남겨줘."),
    CHANGE_TITLE("제목 바꿔줘", "제목을 더 직관적이고 이해하기 쉽게 바꿔줘."),
    TRANSLATE_EN("영어로 번역", "내용을 영어로 번역해줘. 제목과 본문 모두 영어로 작성해줘.")
}

sealed interface AgentSessionIntent {
    data class TopicQueryChanged(val query: String) : AgentSessionIntent
    data object Start : AgentSessionIntent
    data class ToggleItemSelection(val itemId: Long) : AgentSessionIntent
    data object SelectAllRecommended : AgentSessionIntent
    data object ClearSelection : AgentSessionIntent
    data object Next : AgentSessionIntent
    data object Retry : AgentSessionIntent
    data class SelectAction(val index: Int) : AgentSessionIntent
    data object BackToItemSelection : AgentSessionIntent
    data object ConfirmExecution : AgentSessionIntent
    data object CancelExecution : AgentSessionIntent
    data object FinishObservation : AgentSessionIntent
    data object RunAnotherAction : AgentSessionIntent
    data class StartWithSuggestedTopic(val topicQuery: String) : AgentSessionIntent
    data class StartWithClusterItems(val topicQuery: String, val clusterItemIds: List<Long>) : AgentSessionIntent
    data class RefineFeedbackChanged(val feedback: String) : AgentSessionIntent
    data object StartRefinement : AgentSessionIntent
    data class QuickRefine(val action: QuickRefineAction) : AgentSessionIntent
    data object CancelRefinement : AgentSessionIntent
    data object Reset : AgentSessionIntent
    data object DismissError : AgentSessionIntent
}
