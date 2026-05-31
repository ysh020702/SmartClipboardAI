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
    data class RefineFeedbackChanged(val feedback: String) : AgentSessionIntent
    data object StartRefinement : AgentSessionIntent
    data object CancelRefinement : AgentSessionIntent
    data object Reset : AgentSessionIntent
    data object DismissError : AgentSessionIntent
}