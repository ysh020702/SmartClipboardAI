package com.samsung.smartclipboard.presentation.agent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.AgentSessionState

@Composable
fun AgentSessionScreen(
    viewModel: AgentSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    AgentSessionScreen(uiState = uiState, onIntent = viewModel::onIntent)
}

@Composable
fun AgentSessionScreen(
    uiState: AgentSessionUiState,
    onIntent: (AgentSessionIntent) -> Unit
) {
    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState.agentState) {
                is AgentSessionState.Idle -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        TopicInputScreen(
                            topicQuery = uiState.topicQuery,
                            isLoading = uiState.isLoading,
                            errorMessage = uiState.errorMessage,
                            onTopicQueryChange = { onIntent(AgentSessionIntent.TopicQueryChanged(it)) },
                            onStart = { onIntent(AgentSessionIntent.Start) }
                        )
                        ClusterSuggestionScreen(
                            onTopicSelected = { suggestedTitle ->
                                onIntent(AgentSessionIntent.StartWithSuggestedTopic(suggestedTitle))
                            },
                            modifier = Modifier.heightIn(max = 400.dp),
                            compactMode = true
                        )
                    }
                }
                is AgentSessionState.PlanningRetrieval -> TopicInputScreen(
                    topicQuery = uiState.topicQuery, isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage,
                    onTopicQueryChange = { onIntent(AgentSessionIntent.TopicQueryChanged(it)) },
                    onStart = { onIntent(AgentSessionIntent.Start) }
                )
                is AgentSessionState.RetrievingItems -> RetrievingContent(query = state.query, progress = state.progress)
                is AgentSessionState.AwaitingItemSelection -> CandidateItemSelectionScreen(
                    candidateItems = state.candidateItems, selectedItemIds = state.selectedItemIds,
                    recommendationReason = state.recommendationReason, suggestedQueries = state.suggestedQueries,
                    isLoading = uiState.isLoading, errorMessage = uiState.errorMessage,
                    onToggleItem = { onIntent(AgentSessionIntent.ToggleItemSelection(it)) },
                    onSelectAll = { onIntent(AgentSessionIntent.SelectAllRecommended) },
                    onClearSelection = { onIntent(AgentSessionIntent.ClearSelection) },
                    onNext = { onIntent(AgentSessionIntent.Next) },
                    onReset = { onIntent(AgentSessionIntent.Reset) }
                )
                is AgentSessionState.GeneratingActions -> GeneratingActionsContent(selectedItemCount = state.selectedItemCount)
                is AgentSessionState.AwaitingActionSelection -> ActionCandidateScreen(
                    actionDrafts = state.actionDrafts, selectedActionIndex = state.selectedActionIndex,
                    isLoading = uiState.isLoading, errorMessage = uiState.errorMessage,
                    onSelectAction = { onIntent(AgentSessionIntent.SelectAction(it)) },
                    onNext = { onIntent(AgentSessionIntent.Next) },
                    onReset = { onIntent(AgentSessionIntent.Reset) },
                    refineFeedback = uiState.refineFeedback,
                    onRefineFeedbackChange = { onIntent(AgentSessionIntent.RefineFeedbackChanged(it)) },
                    onStartRefinement = { onIntent(AgentSessionIntent.StartRefinement) },
                    onQuickRefine = { onIntent(AgentSessionIntent.QuickRefine(it)) },
                    onCancelRefinement = { onIntent(AgentSessionIntent.CancelRefinement) }
                )
                is AgentSessionState.Refining -> RefiningContent(feedback = state.feedback)
                is AgentSessionState.RoutingTool -> RoutingContent(action = state.action)
                is AgentSessionState.AwaitingExecutionConfirm -> ExecutionConfirmSheet(
                    action = state.action, toolSpec = state.toolSpec, resolvedPayload = state.resolvedPayload,
                    isLoading = uiState.isLoading, errorMessage = uiState.errorMessage,
                    onExecute = { onIntent(AgentSessionIntent.ConfirmExecution) },
                    onCancel = { onIntent(AgentSessionIntent.CancelExecution) },
                    onReset = { onIntent(AgentSessionIntent.Reset) }
                )
                is AgentSessionState.Executing -> ExecutingContent(action = state.action)
                is AgentSessionState.Observing -> {
                    val session = uiState.session
                    ObservationLogScreen(
                        currentResult = state.result,
                        toolResults = session?.toolResults.orEmpty(),
                        isCompleted = false,
                        errorMessage = uiState.errorMessage,
                        onFinish = { onIntent(AgentSessionIntent.FinishObservation) },
                        onRunAnotherAction = { onIntent(AgentSessionIntent.RunAnotherAction) },
                        onReset = { onIntent(AgentSessionIntent.Reset) }
                    )
                }
                is AgentSessionState.Completed -> {
                    val session = uiState.session
                    ObservationLogScreen(
                        currentResult = null,
                        toolResults = session?.toolResults.orEmpty(),
                        isCompleted = true,
                        errorMessage = uiState.errorMessage,
                        onFinish = { onIntent(AgentSessionIntent.Reset) },
                        onRunAnotherAction = { onIntent(AgentSessionIntent.RunAnotherAction) },
                        onReset = { onIntent(AgentSessionIntent.Reset) }
                    )
                }
                is AgentSessionState.Failed -> FailedContent(
                    step = state.step, message = state.message, recoverable = state.recoverable,
                    onRetry = { onIntent(AgentSessionIntent.Retry) },
                    onReset = { onIntent(AgentSessionIntent.Reset) }
                )
                else -> PlaceholderContent(title = "준비 중", message = "이 단계는 아직 구현되지 않았습니다.", onReset = { onIntent(AgentSessionIntent.Reset) })
            }
        }
    }
}

@Composable private fun RetrievingContent(query: String, progress: Float) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("관련 데이터 검색 중...", style = MaterialTheme.typography.headlineSmall)
        Text("\"$query\"", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
        CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
    }
}

@Composable private fun PlaceholderContent(title: String, message: String, onReset: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 16.dp))
        Button(onClick = onReset, modifier = Modifier.padding(top = 24.dp)) { Text("처음으로") }
    }
}

@Composable private fun GeneratingActionsContent(selectedItemCount: Int) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
        Text("선택한 아이템으로 가능한 작업을 생성하고 있습니다.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 16.dp))
        Text("선택된 아이템: ${selectedItemCount}개", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable private fun RefiningContent(feedback: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
        Text(
            text = "AI 보완 중",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "사용자 피드백을 반영해 작업 후보를 다시 정리하고 있습니다.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "\"${feedback.take(200)}\"",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable private fun RoutingContent(action: AgentActionDraft) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
        Text("선택한 작업에 맞는 도구를 찾고 있습니다.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 16.dp))
        Text(action.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable private fun ExecutingContent(action: AgentActionDraft) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
        Text("선택한 도구를 실행하고 있습니다.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 16.dp))
        Text(action.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun FailedContent(
    step: String, message: String, recoverable: Boolean,
    onRetry: () -> Unit, onReset: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("오류 발생", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
        Text("[$step] $message", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 16.dp))
        if (recoverable) { Button(onClick = onRetry, modifier = Modifier.padding(top = 24.dp)) { Text("재시도") } }
        Button(onClick = onReset, modifier = Modifier.padding(top = 8.dp)) { Text("처음으로") }
    }
}
