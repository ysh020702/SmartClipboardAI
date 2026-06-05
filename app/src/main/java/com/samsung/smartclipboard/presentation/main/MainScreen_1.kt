package com.samsung.smartclipboard.presentation.main

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.DataItemType
import com.samsung.smartclipboard.domain.model.Topic
import com.samsung.smartclipboard.domain.model.TopicAction
import com.samsung.smartclipboard.domain.model.TopicActionStatus
import com.samsung.smartclipboard.domain.model.TopicActionType
import com.samsung.smartclipboard.domain.model.TopicAnalysis
import com.samsung.smartclipboard.presentation.agent.AgentSessionIntent
import com.samsung.smartclipboard.presentation.agent.AgentSessionScreen
import com.samsung.smartclipboard.presentation.agent.AgentSessionViewModel
import com.samsung.smartclipboard.presentation.agent.ClusterSuggestionScreen
import com.samsung.smartclipboard.presentation.agent.ClusterSuggestionViewModel
import com.samsung.smartclipboard.ui.theme.AppColors
import com.samsung.smartclipboard.ui.theme.BlueGradient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit,
    onRequestMediaPermission: () -> Unit = {},
    onShareDraft: (title: String, body: String) -> Unit = { _, _ -> },
    onCreateCalendarDraft: (title: String, description: String) -> Unit = { _, _ -> }
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onIntent(MainIntent.SnackbarShown)
        }
    }

    LaunchedEffect(uiState.handoffMessage) {
        uiState.handoffMessage?.let {
            snackbarHostState.showSnackbar(it)
            onIntent(MainIntent.DismissHandoffMessage)
        }
    }

    BackHandler(enabled = uiState.screenMode != MainScreenMode.HOME) {
        onIntent(MainIntent.BackToHome)
    }

    if (uiState.pendingDeleteItemId != null) {
        AlertDialog(
            onDismissRequest = { onIntent(MainIntent.DismissDeleteDialog) },
            title = { Text("데이터를 삭제할까요?") },
            confirmButton = {
                TextButton(onClick = { onIntent(MainIntent.ConfirmDeleteItem) }) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(MainIntent.DismissDeleteDialog) }) {
                    Text("취소")
                }
            }
        )
    }

    if (uiState.showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { onIntent(MainIntent.DismissClearAllDialog) },
            title = { Text("모든 데이터를 지울까요?") },
            text = { Text("수집된 데이터와 추천 항목이 이 기기에서 삭제됩니다.") },
            confirmButton = {
                TextButton(onClick = { onIntent(MainIntent.ConfirmClearAll) }) {
                    Text("전체 삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(MainIntent.DismissClearAllDialog) }) {
                    Text("취소")
                }
            }
        )
    }

    if (uiState.showHandoffSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { onIntent(MainIntent.DismissHandoffSheet) },
            sheetState = sheetState
        ) {
            TopicPipelineSheet(
                uiState = uiState,
                onIntent = onIntent
            )
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            UnifiedTopBar(uiState = uiState, onIntent = onIntent)
        },
        bottomBar = {
            if (uiState.screenMode == MainScreenMode.MANUAL_SELECT && uiState.isSelectionMode) {
                SelectionActionBar(
                    selectedCount = uiState.selectedItemIds.size,
                    onCancel = { onIntent(MainIntent.ExitSelectionMode) },
                    onNext = {
                        onIntent(MainIntent.StartAgentWithManualSelection(
                            uiState.handoffTitle,
                            uiState.selectedItemIds.toList()
                        ))
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.screenMode) {
                MainScreenMode.HOME -> UnifiedHomeScreen(
                    uiState = uiState,
                    onIntent = onIntent
                )

                MainScreenMode.AI_SEARCH -> AgentSessionScreen()

                MainScreenMode.MANUAL_SELECT -> ManualSelectScreen(
                    uiState = uiState,
                    onIntent = onIntent
                )

                MainScreenMode.DATA_BROWSER -> DataBrowser(
                    uiState = uiState,
                    onIntent = onIntent
                )

                MainScreenMode.TOPIC_DETAIL -> TopicDetailScreen(
                    uiState = uiState,
                    onIntent = onIntent,
                    onShareDraft = onShareDraft,
                    onCreateCalendarDraft = onCreateCalendarDraft
                )

                MainScreenMode.AGENT -> AgentSessionWithItemsScreen(
                    topicQuery = uiState.handoffTitle,
                    selectedItemIds = uiState.selectedItemIds.toList()
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Unified Top Bar
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnifiedTopBar(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit
) {
    val showBack = uiState.screenMode != MainScreenMode.HOME

    TopAppBar(
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = { onIntent(MainIntent.BackToHome) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로"
                    )
                }
            }
        },
        title = {
            Text(
                text = when (uiState.screenMode) {
                    MainScreenMode.HOME -> "Smart Clipboard AI"
                    MainScreenMode.AI_SEARCH -> "AI 찾기"
                    MainScreenMode.MANUAL_SELECT -> if (uiState.isSelectionMode) "데이터 선택" else "직접 고르기"
                    MainScreenMode.DATA_BROWSER -> "수집 데이터"
                    MainScreenMode.TOPIC_DETAIL -> uiState.topics
                        .firstOrNull { it.id == uiState.selectedTopicId }
                        ?.title
                        ?: "주제 상세"
                    MainScreenMode.AGENT -> "AI 에이전트"
                }
            )
        },
        actions = {
            when (uiState.screenMode) {
                MainScreenMode.MANUAL_SELECT -> {
                    if (uiState.isSelectionMode) {
                        TextButton(onClick = { onIntent(MainIntent.ExitSelectionMode) }) {
                            Text("취소")
                        }
                    } else if (uiState.totalCount > 0) {
                        TextButton(onClick = { onIntent(MainIntent.EnterSelectionMode) }) {
                            Text("선택")
                        }
                    }
                }
                MainScreenMode.DATA_BROWSER -> {
                    if (uiState.totalCount > 0) {
                        TextButton(onClick = { onIntent(MainIntent.RequestClearAll) }) {
                            Text("전체 삭제", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                MainScreenMode.TOPIC_DETAIL -> {
                    if (uiState.selectedTopicItems.isNotEmpty()) {
                        TextButton(
                            onClick = { onIntent(MainIntent.RunSelectedTopicAnalysis) },
                            enabled = !uiState.isRunningTopicAnalysis
                        ) {
                            Text("분석")
                        }
                    }
                }
                else -> {}
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = AppColors.Slate800,
            actionIconContentColor = AppColors.Blue,
            navigationIconContentColor = AppColors.Blue
        )
    )
}


