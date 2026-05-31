package com.samsung.smartclipboard.presentation.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samsung.smartclipboard.domain.agent.ActionPlanner
import com.samsung.smartclipboard.domain.agent.ItemRecommendationAgent
import com.samsung.smartclipboard.domain.agent.RefineAgent
import com.samsung.smartclipboard.domain.agent.TopicPlanner
import com.samsung.smartclipboard.domain.model.AgentSession
import com.samsung.smartclipboard.domain.model.AgentSessionState
import com.samsung.smartclipboard.domain.model.RetrievalPlan
import com.samsung.smartclipboard.domain.retrieval.CandidateItemRanker
import com.samsung.smartclipboard.domain.retrieval.DataRetriever
import com.samsung.smartclipboard.domain.tool.ToolExecutor
import com.samsung.smartclipboard.domain.tool.ToolRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AgentSessionViewModel @Inject constructor(
    private val topicPlanner: TopicPlanner,
    private val dataRetriever: DataRetriever,
    private val candidateItemRanker: CandidateItemRanker,
    private val itemRecommendationAgent: ItemRecommendationAgent,
    private val actionPlanner: ActionPlanner,
    private val toolRouter: ToolRouter,
    private val toolExecutor: ToolExecutor,
    private val refineAgent: RefineAgent
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentSessionUiState())
    val uiState: StateFlow<AgentSessionUiState> = _uiState.asStateFlow()

    fun onIntent(intent: AgentSessionIntent) {
        when (intent) {
            is AgentSessionIntent.TopicQueryChanged -> onTopicQueryChanged(intent.query)
            is AgentSessionIntent.Start -> startSession()
            is AgentSessionIntent.StartWithSuggestedTopic -> startSessionWithSuggestedTopic(intent.topicQuery)
            is AgentSessionIntent.ToggleItemSelection -> toggleItemSelection(intent.itemId)
            is AgentSessionIntent.SelectAllRecommended -> selectAllRecommended()
            is AgentSessionIntent.ClearSelection -> clearSelection()
            is AgentSessionIntent.Next -> handleNext()
            is AgentSessionIntent.SelectAction -> selectAction(intent.index)
            is AgentSessionIntent.BackToItemSelection -> backToItemSelection()
            is AgentSessionIntent.ConfirmExecution -> confirmExecution()
            is AgentSessionIntent.CancelExecution -> cancelExecution()
            is AgentSessionIntent.FinishObservation -> finishObservation()
            is AgentSessionIntent.RunAnotherAction -> runAnotherAction()
            is AgentSessionIntent.RefineFeedbackChanged -> onRefineFeedbackChanged(intent.feedback)
            is AgentSessionIntent.StartRefinement -> startRefinement()
            is AgentSessionIntent.CancelRefinement -> onCancelRefinement()
            is AgentSessionIntent.Retry -> startSession()
            is AgentSessionIntent.Reset -> resetToIdle()
            is AgentSessionIntent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    // --- basic state handlers ---
    private fun onTopicQueryChanged(query: String) { _uiState.update { it.copy(topicQuery = query, errorMessage = null) } }
    private fun onRefineFeedbackChanged(feedback: String) { _uiState.update { it.copy(refineFeedback = feedback.take(1500)) } }
    private fun onCancelRefinement() {
        val current = _uiState.value
        if (current.isLoading && current.agentState is AgentSessionState.Refining) {
            _uiState.update { it.copy(errorMessage = "보완이 진행 중입니다.") }; return
        }
        _uiState.update { it.copy(refineFeedback = "", errorMessage = null) }
    }

    // --- start session ---
    private fun startSession() { startSession(topicOverride = null) }
    private fun startSessionWithSuggestedTopic(topic: String) {
        val normalized = topic.trim()
        if (normalized.isBlank()) { _uiState.update { it.copy(agentState = AgentSessionState.Failed("input", "추천 주제가 비어 있습니다.", true), errorMessage = "추천 주제가 비어 있습니다.", isLoading = false) }; return }
        _uiState.update { it.copy(topicQuery = normalized) }
        startSession(topicOverride = normalized)
    }

    private fun startSession(topicOverride: String?) {
        val current = _uiState.value
        if (current.isLoading) return
        val topicQuery = (topicOverride ?: current.topicQuery).trim()
        if (topicQuery.isBlank()) { _uiState.update { it.copy(agentState = AgentSessionState.Failed("input", "주제를 입력해 주세요.", true), errorMessage = "주제를 입력해 주세요.", isLoading = false) }; return }
        val session = AgentSession(sessionId = UUID.randomUUID().toString(), topicTitle = topicQuery, state = AgentSessionState.PlanningRetrieval)
        _uiState.update { it.copy(session = session, agentState = AgentSessionState.PlanningRetrieval, isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val plan = topicPlanner.plan(topicQuery).getOrElse { throw it }
                _uiState.update { it.copy(retrievalPlan = plan, agentState = AgentSessionState.RetrievingItems(query = topicQuery, progress = 0.35f)) }
                val items = dataRetriever.retrieve(plan)
                val rankedCandidates = candidateItemRanker.rank(topicQuery, plan, items)
                val result = itemRecommendationAgent.recommend(topicQuery, plan, rankedCandidates).getOrElse { throw it }
                val awaitState = AgentSessionState.AwaitingItemSelection(result.recommendedItems, result.recommendationReason, result.suggestedQueries, result.selectedItemIds)
                val updatedSession = session.copy(state = awaitState, candidateItems = result.recommendedItems, updatedAt = System.currentTimeMillis())
                _uiState.update { it.copy(session = updatedSession, agentState = awaitState, isLoading = false, errorMessage = null) }
            } catch (e: Exception) { setFailed("planning", e) }
        }
    }

    // --- selection / action planning / routing / execution --- (existing code kept compact)
    private fun toggleItemSelection(itemId: Long) {
        val cur = _uiState.value; val st = cur.agentState
        if (st !is AgentSessionState.AwaitingItemSelection || itemId !in st.candidateItems.map { it.item.id }.toSet()) return
        val ns = st.copy(selectedItemIds = if (itemId in st.selectedItemIds) st.selectedItemIds - itemId else st.selectedItemIds + itemId)
        _uiState.update { it.copy(session = cur.session?.copy(state = ns, updatedAt = System.currentTimeMillis()), agentState = ns) }
    }
    private fun selectAllRecommended() {
        val cur = _uiState.value; val st = cur.agentState
        if (st !is AgentSessionState.AwaitingItemSelection) return
        val ns = st.copy(selectedItemIds = st.candidateItems.map { it.item.id }.toSet())
        _uiState.update { it.copy(session = cur.session?.copy(state = ns, updatedAt = System.currentTimeMillis()), agentState = ns) }
    }
    private fun clearSelection() {
        val cur = _uiState.value; val st = cur.agentState
        if (st !is AgentSessionState.AwaitingItemSelection) return
        val ns = st.copy(selectedItemIds = emptySet())
        _uiState.update { it.copy(session = cur.session?.copy(state = ns, updatedAt = System.currentTimeMillis()), agentState = ns) }
    }
    private fun handleNext() { when (_uiState.value.agentState) { is AgentSessionState.AwaitingItemSelection -> generateActionsFromSelection(); is AgentSessionState.AwaitingActionSelection -> routeSelectedAction(); else -> {} } }
    private fun selectAction(index: Int) {
        val cur = _uiState.value; val st = cur.agentState
        if (st !is AgentSessionState.AwaitingActionSelection || index !in st.actionDrafts.indices) return
        val ns = st.copy(selectedActionIndex = index)
        _uiState.update { it.copy(session = cur.session?.copy(state = ns, selectedActionIndex = index, updatedAt = System.currentTimeMillis()), agentState = ns, errorMessage = null) }
    }
    private fun backToItemSelection() {
        val cur = _uiState.value; val st = cur.agentState
        if (st !is AgentSessionState.AwaitingActionSelection) return
        val prev = cur.session?.candidateItems?.let { AgentSessionState.AwaitingItemSelection(it, "", emptyList(), st.actionDrafts.getOrNull(st.selectedActionIndex ?: -1)?.sourceItemIds?.toSet() ?: emptySet()) } ?: return
        _uiState.update { it.copy(session = cur.session?.copy(state = prev, updatedAt = System.currentTimeMillis()), agentState = prev, isLoading = false, errorMessage = null) }
    }

    private fun generateActionsFromSelection() {
        val cur = _uiState.value; val st = cur.agentState
        if (st !is AgentSessionState.AwaitingItemSelection) return
        val sIds = st.selectedItemIds
        if (sIds.isEmpty()) { _uiState.update { it.copy(errorMessage = "하나 이상의 아이템을 선택해 주세요.") }; return }
        val sItems = st.candidateItems.filter { it.item.id in sIds }
        if (sItems.isEmpty()) { _uiState.update { it.copy(errorMessage = "선택된 아이템을 찾을 수 없습니다.") }; return }
        val tq = cur.topicQuery.trim()
        val pl = cur.retrievalPlan ?: RetrievalPlan(keywords = listOf(tq), maxResults = sItems.size.coerceAtLeast(5))
        val gs = AgentSessionState.GeneratingActions(selectedItemCount = sIds.size)
        _uiState.update { it.copy(session = cur.session?.copy(state = gs, updatedAt = System.currentTimeMillis()), agentState = gs, isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val acts = actionPlanner.planActions(tq, pl, sItems).getOrElse { throw it }
                if (acts.isEmpty()) { setFailed("action_planning", "작업 후보 생성 실패"); return@launch }
                val idSet = sItems.map { it.item.id }.toSet()
                val va = acts.filter { it.sourceItemIds.all { id -> id in idSet } && it.sourceItemIds.isNotEmpty() }.distinctBy { it.type to it.title }.take(5)
                if (va.isEmpty()) { setFailed("action_planning", "유효한 작업 후보 없음"); return@launch }
                val as_ = AgentSessionState.AwaitingActionSelection(actionDrafts = va, selectedActionIndex = va.indices.firstOrNull())
                _uiState.update { it.copy(session = cur.session?.copy(state = as_, actionDrafts = va, selectedActionIndex = as_.selectedActionIndex, updatedAt = System.currentTimeMillis()), agentState = as_, isLoading = false, errorMessage = null) }
            } catch (e: Exception) { setFailed("action_planning", e) }
        }
    }

    private fun routeSelectedAction() { /* existing */ }
    private fun confirmExecution() { /* existing */ }
    private fun cancelExecution() { /* existing */ }
    private fun finishObservation() { /* existing */ }
    private fun runAnotherAction() { /* existing */ }

    // --- M9A Refinement ---
    private fun startRefinement() {
        val cur = _uiState.value
        if (cur.isLoading) return
        val st = cur.agentState
        if (st !is AgentSessionState.AwaitingActionSelection) return
        val feedback = cur.refineFeedback.trim()
        if (feedback.isBlank()) { _uiState.update { it.copy(errorMessage = "보완 요청 내용을 입력해 주세요.") }; return }
        val session = cur.session ?: return
        val tq = cur.topicQuery.trim()
        val pl = cur.retrievalPlan ?: RetrievalPlan(keywords = listOf(tq), maxResults = 20)
        val currentActions = st.actionDrafts
        val selectedIds = currentActions.flatMap { it.sourceItemIds }.toSet()
        val sItems = session.candidateItems.filter { it.item.id in selectedIds }
        if (sItems.isEmpty()) { _uiState.update { it.copy(errorMessage = "보완할 선택 아이템을 찾지 못했습니다.") }; return }

        val previousState = st
        val refiningState = AgentSessionState.Refining(feedback)
        _uiState.update { it.copy(session = session.copy(state = refiningState, updatedAt = System.currentTimeMillis()), agentState = refiningState, isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val refinedActions = refineAgent.refineActions(tq, pl, sItems, currentActions, feedback).getOrElse { throw it }
                if (refinedActions.isEmpty()) {
                    _uiState.update { it.copy(session = session.copy(state = previousState, updatedAt = System.currentTimeMillis()), agentState = previousState, isLoading = false, errorMessage = "AI 보완에 실패했습니다. 기존 작업 후보를 유지합니다.") }
                    return@launch
                }
                val ridSet = sItems.map { it.item.id }.toSet()
                val va = refinedActions.filter { it.sourceItemIds.all { id -> id in ridSet } && it.sourceItemIds.isNotEmpty() }.distinctBy { it.type to it.title }.take(5)
                if (va.isEmpty()) {
                    _uiState.update { it.copy(session = session.copy(state = previousState, updatedAt = System.currentTimeMillis()), agentState = previousState, isLoading = false, errorMessage = "보완된 작업 후보가 유효하지 않습니다.") }
                    return@launch
                }
                val as_ = AgentSessionState.AwaitingActionSelection(actionDrafts = va, selectedActionIndex = va.indices.firstOrNull())
                _uiState.update { it.copy(session = session.copy(state = as_, actionDrafts = va, selectedActionIndex = as_.selectedActionIndex, updatedAt = System.currentTimeMillis()), agentState = as_, refineFeedback = "", isLoading = false, errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(session = session.copy(state = previousState, updatedAt = System.currentTimeMillis()), agentState = previousState, isLoading = false, errorMessage = "AI 보완에 실패했습니다. 기존 작업 후보를 유지합니다.") }
            }
        }
    }

    // --- helpers ---
    private fun resetToIdle() { _uiState.update { AgentSessionUiState(topicQuery = it.topicQuery) } }
    private fun setFailed(step: String, message: String) {
        val cur = _uiState.value
        _uiState.update { it.copy(session = cur.session?.copy(state = AgentSessionState.Failed(step, message, true), updatedAt = System.currentTimeMillis()), agentState = AgentSessionState.Failed(step, message, true), isLoading = false, errorMessage = message) }
    }
    private fun setFailed(step: String, e: Throwable) { setFailed(step, e.message ?: "알 수 없는 오류") }
}