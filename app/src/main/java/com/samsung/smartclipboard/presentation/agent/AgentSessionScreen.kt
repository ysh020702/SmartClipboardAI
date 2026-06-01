package com.samsung.smartclipboard.presentation.agent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.AgentSessionState
import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.DataItemType
import java.util.Calendar

@Composable
fun AgentSessionScreen(
    viewModel: AgentSessionViewModel = hiltViewModel(),
    onRequestMediaPermission: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    AgentSessionScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onRequestMediaPermission = onRequestMediaPermission
    )
}

@Composable
fun AgentSessionScreen(
    uiState: AgentSessionUiState,
    onIntent: (AgentSessionIntent) -> Unit,
    onRequestMediaPermission: () -> Unit = {}
) {
    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState.agentState) {
                is AgentSessionState.Idle -> {
                    DashboardScreen(
                        uiState = uiState,
                        onIntent = onIntent,
                        onRequestMediaPermission = onRequestMediaPermission
                    )
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

// ---------------------------------------------------------------------------
// Dashboard Home (Idle state) — 이전 MainScreen.DashboardHome 구성 요소
// ---------------------------------------------------------------------------
@Composable
private fun DashboardScreen(
    uiState: AgentSessionUiState,
    onIntent: (AgentSessionIntent) -> Unit,
    onRequestMediaPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TodayCollectionCard(uiState = uiState, onIntent = onIntent)

        ScreenshotImportBanner(
            uiState = uiState,
            onIntent = onIntent,
            onRequestMediaPermission = onRequestMediaPermission
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

@Composable
private fun TodayCollectionCard(
    uiState: AgentSessionUiState,
    onIntent: (AgentSessionIntent) -> Unit
) {
    val todayItems = uiState.items.filter { isSameDay(it.createdAt, 0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "오늘 수집",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${todayItems.size}개 수집됨",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = todaySuggestionText(todayItems),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScreenshotImportBanner(
    uiState: AgentSessionUiState,
    onIntent: (AgentSessionIntent) -> Unit,
    onRequestMediaPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "스크린샷 가져오기",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (uiState.showMediaPermissionBanner) {
                Text(
                    text = "이미지 접근을 허용하면 최근 스크린샷을 수집할 수 있어요.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onRequestMediaPermission) {
                    Text("권한 허용")
                }
            } else if (uiState.isMediaImporting) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "스크린샷을 살펴보는 중...",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Text(
                    text = "최근 스크린샷을 다시 확인합니다.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onIntent(AgentSessionIntent.ImportRecentScreenshots) }) {
                    Text("다시 스캔")
                }
            }
            uiState.mediaImportMessage?.let { message ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onIntent(AgentSessionIntent.DismissMediaImportMessage) }) {
                        Text("닫기", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 기존 AgentSessionScreen 하위 컴포넌트 (변경 없음)
// ---------------------------------------------------------------------------

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

// ---------------------------------------------------------------------------
// 유틸리티 함수
// ---------------------------------------------------------------------------
private fun todaySuggestionText(todayItems: List<DataItem>): String {
    if (todayItems.isEmpty()) {
        return "오늘 모인 데이터가 생기면 요약, 일정, 작업 연결 후보를 추천합니다."
    }
    val hasDateLikeText = todayItems.any {
        it.content.contains(Regex("\\d{1,2}:\\d{2}|\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}|오전|오후"))
    }
    val linkCount = todayItems.count { it.type == DataItemType.LINK }
    val screenshotCount = todayItems.count { it.type == DataItemType.SCREENSHOT }
    return when {
        hasDateLikeText -> "날짜나 시간이 들어간 데이터가 있어 일정 초안으로 이어질 수 있어요."
        screenshotCount >= 2 -> "스크린샷이 여러 개 모였어요. 관련 장면을 묶어 검토해보세요."
        linkCount >= 2 -> "링크가 여러 개 모였어요. 리서치 노트로 정리하기 좋습니다."
        else -> "필요한 데이터를 직접 고르거나 주제를 입력해 AI 후보 선택을 시작하세요."
    }
}

private fun isSameDay(timeMillis: Long, dayOffset: Int): Boolean {
    val target = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, dayOffset)
    }
    val itemDay = Calendar.getInstance().apply {
        timeInMillis = timeMillis
    }
    return target.get(Calendar.YEAR) == itemDay.get(Calendar.YEAR) &&
            target.get(Calendar.DAY_OF_YEAR) == itemDay.get(Calendar.DAY_OF_YEAR)
}