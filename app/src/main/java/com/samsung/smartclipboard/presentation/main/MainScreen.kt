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
import kotlin.text.format

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

// ---------------------------------------------------------------------------
// Unified Home Screen (AI 메뉴 상단 부분)
// ---------------------------------------------------------------------------

@Composable
private fun UnifiedHomeScreen(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero header card (AI 메뉴 위쪽 부분)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BlueGradient)
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(54.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "SmartClipboardAI",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                "수집한 정보를 AI로 정리하고 실행합니다",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.72f)
            )

            Spacer(Modifier.height(36.dp))

            // Main card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(BlueGradient)
                        .padding(20.dp)
                ) {
                    Text(
                        "무엇을 정리할까요?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "원하는 주제를 입력하거나 AI에게 맡겨보세요.",
                        color = Color(0xFFC7D2FE).copy(alpha = 0.78f),
                        fontSize = 11.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    TextField(
                        value = uiState.topicQuery,
                        onValueChange = { onIntent(MainIntent.UpdateTopicQuery(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "예: 회의 자료, 여행 링크, 일정 캡처",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 13.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.14f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.14f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                        ),
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onIntent(MainIntent.OpenAiSearch) },
                            enabled = uiState.items.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = AppColors.Blue
                            ),
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "AI 찾기",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onIntent(MainIntent.OpenManualSelect) },
                            enabled = uiState.items.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.18f),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                        ) {
                            Text("직접 고르기", fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { onIntent(MainIntent.OpenDataBrowserFromHome) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.08f),
                            contentColor = Color(0xFFC7D2FE)
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    ) {
                        Text("수집 현황", fontSize = 12.sp)
                        Spacer(Modifier.width(6.dp))
                        Pill(
                            "${uiState.totalCount}개",
                            bg = Color.White.copy(alpha = 0.14f),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Today collection summary
        if (uiState.items.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DividerLine(Modifier.weight(1f))
                Text(
                    "오늘 ${uiState.totalCount}개의 항목을 수집했어요",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = AppColors.Slate400,
                    fontSize = 10.sp
                )
                DividerLine(Modifier.weight(1f))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// AI Search Screen (AI 찾기 → 클러스터 주제 추천)
// ---------------------------------------------------------------------------

@Composable
private fun AiSearchScreen(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit
) {
    val clusterViewModel: ClusterSuggestionViewModel = hiltViewModel()
    val clusterUiState by clusterViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Compact header (not scrollable on its own)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BlueGradient)
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "SmartClipboardAI",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.10f)
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
            ) {
                Column(
                    modifier = Modifier
                        .background(SolidColor(Color.Transparent))
                        .padding(20.dp)
                ) {
                    Text(
                        "무엇을 정리할까요?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "원하는 주제를 입력하거나 AI에게 맡겨보세요.",
                        color = Color(0xFFC7D2FE).copy(alpha = 0.78f),
                        fontSize = 11.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    TextField(
                        value = uiState.topicQuery,
                        onValueChange = { onIntent(MainIntent.UpdateTopicQuery(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "예: 회의 자료, 여행 링크, 일정 캡처",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 13.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.14f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.14f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                        ),
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onIntent(MainIntent.OpenAiSearch) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = AppColors.Blue
                            ),
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("AI 찾기", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onIntent(MainIntent.OpenManualSelect) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.18f),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                        ) {
                            Text("직접 고르기", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Cluster suggestion section (AI 메뉴 아래쪽 부분)
        // Use compactMode to avoid Scaffold-inside-Column sizing issues
        ClusterSuggestionScreen(
            uiState = clusterUiState,
            onIntent = clusterViewModel::onIntent,
            onTopicSelected = { suggestedTitle, clusterItemIds ->
                onIntent(MainIntent.StartAgentWithCluster(suggestedTitle, clusterItemIds))
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            compactMode = true
        )
    }
}

// ---------------------------------------------------------------------------
// Manual Select Screen (직접 고르기 → 데이터 선택 + 주제 입력)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ManualSelectScreen(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Topic input section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "주제를 입력하세요",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "직접 주제를 정하고 데이터를 선택합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.handoffTitle,
                    onValueChange = { onIntent(MainIntent.UpdateHandoffTitle(it)) },
                    label = { Text("주제명") },
                    placeholder = { Text("예: SmartClipboard UX 설계") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Data selection section
        Text(
            text = "${uiState.totalCount}개 전체 · 텍스트 ${uiState.textCount} · 링크 ${uiState.linkCount} · 이미지 ${uiState.imageCount} · 스크린샷 ${uiState.screenshotCount}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MainFilter.entries.forEach { filter ->
                FilterChip(
                    selected = uiState.selectedFilter == filter,
                    onClick = { onIntent(MainIntent.SelectFilter(filter)) },
                    label = { Text(filter.displayName()) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        if (uiState.isSelectionMode) {
            Spacer(modifier = Modifier.height(12.dp))
            SelectionSummaryCard(
                selectedCount = uiState.selectedItemIds.size,
                hiddenSelectedCount = 0,
                onSelectVisible = { onIntent(MainIntent.SelectAllVisible) },
                onClear = { onIntent(MainIntent.ClearSelection) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            uiState.items.isEmpty() -> {
                EmptyDataMessage(modifier = Modifier.weight(1f))
            }

            uiState.visibleItems.isEmpty() -> {
                Text(
                    text = "현재 필터에 맞는 데이터가 없어요.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp)
                )
            }

            else -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.visibleItems) { item ->
                        ItemCard(
                            item = item,
                            isSelected = item.id in uiState.selectedItemIds,
                            isSelectionMode = true,
                            onToggleSelect = { onIntent(MainIntent.ToggleItemSelection(item.id)) },
                            onDelete = { onIntent(MainIntent.RequestDeleteItem(item.id)) }
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Data Browser (수집 현황)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DataBrowser(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit
) {
    val selectedVisibleCount = uiState.visibleItems.count { it.id in uiState.selectedItemIds }
    val hiddenSelectedCount = (uiState.selectedItemIds.size - selectedVisibleCount).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "${uiState.totalCount}개 전체 · 텍스트 ${uiState.textCount} · 링크 ${uiState.linkCount} · 이미지 ${uiState.imageCount} · 스크린샷 ${uiState.screenshotCount}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MainFilter.entries.forEach { filter ->
                FilterChip(
                    selected = uiState.selectedFilter == filter,
                    onClick = { onIntent(MainIntent.SelectFilter(filter)) },
                    label = { Text(filter.displayName()) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        if (uiState.isSelectionMode) {
            Spacer(modifier = Modifier.height(12.dp))
            SelectionSummaryCard(
                selectedCount = uiState.selectedItemIds.size,
                hiddenSelectedCount = hiddenSelectedCount,
                onSelectVisible = { onIntent(MainIntent.SelectAllVisible) },
                onClear = { onIntent(MainIntent.ClearSelection) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            uiState.items.isEmpty() -> {
                EmptyDataMessage(modifier = Modifier.weight(1f))
            }

            uiState.visibleItems.isEmpty() -> {
                Text(
                    text = "현재 필터에 맞는 데이터가 없어요.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp)
                )
            }

            else -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.visibleItems) { item ->
                        ItemCard(
                            item = item,
                            isSelected = item.id in uiState.selectedItemIds,
                            isSelectionMode = uiState.isSelectionMode,
                            onToggleSelect = { onIntent(MainIntent.ToggleItemSelection(item.id)) },
                            onDelete = { onIntent(MainIntent.RequestDeleteItem(item.id)) }
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Topic Pipeline Sheet
// ---------------------------------------------------------------------------

@Composable
private fun TopicPipelineSheet(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "주제로 묶고 분석 시작",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${uiState.selectedItemIds.size}개 데이터를 하나의 주제에 붙이고 AI Agent 분석 파이프라인으로 넘깁니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        OutlinedTextField(
            value = uiState.handoffTitle,
            onValueChange = { onIntent(MainIntent.UpdateHandoffTitle(it)) },
            label = { Text("주제명") },
            placeholder = { Text("예: SmartClipboard UX 설계") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        uiState.handoffActionMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Button(
            onClick = { onIntent(MainIntent.AddSelectionToTopicAndAnalyze) },
            enabled = uiState.handoffTitle.isNotBlank() && !uiState.isRunningTopicAnalysis,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isRunningTopicAnalysis) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("주제 생성 + 분석 시작")
        }

        OutlinedButton(
            onClick = { onIntent(MainIntent.AddSelectionToTopic) },
            enabled = uiState.handoffTitle.isNotBlank() && !uiState.isRunningTopicAnalysis,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("주제에만 추가")
        }

        TextButton(
            onClick = { onIntent(MainIntent.DismissHandoffSheet) },
            enabled = !uiState.isRunningTopicAnalysis,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("닫기")
        }
    }
}

// ---------------------------------------------------------------------------
// Topic Detail Screen
// ---------------------------------------------------------------------------

@Composable
private fun TopicDetailScreen(
    uiState: MainUiState,
    onIntent: (MainIntent) -> Unit,
    onShareDraft: (title: String, body: String) -> Unit,
    onCreateCalendarDraft: (title: String, description: String) -> Unit
) {
    val topic = uiState.topics.firstOrNull { it.id == uiState.selectedTopicId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TopicDetailHeader(
                title = topic?.title ?: "주제 상세",
                itemCount = uiState.selectedTopicItems.size,
                analysisCount = uiState.selectedTopicAnalysis.size,
                actionCount = uiState.selectedTopicActions.size,
                isRunning = uiState.isRunningTopicAnalysis,
                onAddData = { onIntent(MainIntent.OpenManualSelection) },
                onRunAnalysis = { onIntent(MainIntent.RunSelectedTopicAnalysis) }
            )
        }

        item {
            TopicDetailTabs(
                selectedTab = uiState.selectedTopicTab,
                onSelectTab = { onIntent(MainIntent.SelectTopicDetailTab(it)) }
            )
        }

        when (uiState.selectedTopicTab) {
            TopicDetailTab.MATERIALS -> {
                if (uiState.selectedTopicItems.isEmpty()) {
                    item {
                        EmptyTopicState(
                            title = "연결된 데이터가 없어요",
                            message = "데이터 화면에서 자료를 선택해 이 주제에 추가해 주세요."
                        )
                    }
                } else {
                    items(uiState.selectedTopicItems) { item ->
                        TopicMaterialCard(item = item)
                    }
                }
            }

            TopicDetailTab.ANALYSIS -> {
                if (uiState.isRunningTopicAnalysis) {
                    item {
                        RunningAnalysisCard()
                    }
                }
                if (uiState.selectedTopicAnalysis.isEmpty() && !uiState.isRunningTopicAnalysis) {
                    item {
                        EmptyTopicState(
                            title = "아직 분석 결과가 없어요",
                            message = "선택한 데이터로 요약과 액션 초안을 생성할 수 있어요.",
                            actionLabel = "분석 시작",
                            onAction = { onIntent(MainIntent.RunSelectedTopicAnalysis) }
                        )
                    }
                } else {
                    items(uiState.selectedTopicAnalysis) { analysis ->
                        TopicAnalysisCard(analysis = analysis)
                    }
                }
            }

            TopicDetailTab.ACTIONS -> {
                if (uiState.selectedTopicActions.isEmpty()) {
                    item {
                        EmptyTopicState(
                            title = "생성된 작업이 없어요",
                            message = "분석을 실행하면 일정, 요약, 할 일 초안이 여기에 모입니다.",
                            actionLabel = "분석 시작",
                            onAction = { onIntent(MainIntent.RunSelectedTopicAnalysis) }
                        )
                    }
                } else {
                    items(uiState.selectedTopicActions) { action ->
                        TopicActionCard(
                            action = action,
                            onTitleChange = { title ->
                                onIntent(MainIntent.UpdateTopicActionDraft(action.id, title, action.body))
                            },
                            onBodyChange = { body ->
                                onIntent(MainIntent.UpdateTopicActionDraft(action.id, action.title, body))
                            },
                            onShareDraft = {
                                onShareDraft(action.title, action.body)
                                onIntent(MainIntent.HandoffActionCompleted("공유 초안을 열었어요"))
                            },
                            onCreateCalendarDraft = {
                                onCreateCalendarDraft(action.title, action.body)
                                onIntent(MainIntent.HandoffActionCompleted("캘린더 초안을 열었어요"))
                            }
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared Components
// ---------------------------------------------------------------------------

@Composable
private fun TopicDetailHeader(
    title: String,
    itemCount: Int,
    analysisCount: Int,
    actionCount: Int,
    isRunning: Boolean,
    onAddData: () -> Unit,
    onRunAnalysis: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "자료 ${itemCount}개 · 분석 ${analysisCount}개 · 작업 ${actionCount}개",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onAddData,
                modifier = Modifier.weight(1f)
            ) {
                Text("데이터 추가")
            }
            Button(
                onClick = onRunAnalysis,
                enabled = itemCount > 0 && !isRunning,
                modifier = Modifier.weight(1f)
            ) {
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("분석 실행")
            }
        }
    }
}

@Composable
private fun TopicDetailTabs(
    selectedTab: TopicDetailTab,
    onSelectTab: (TopicDetailTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TopicDetailTab.entries.forEach { tab ->
            FilterChip(
                selected = selectedTab == tab,
                onClick = { onSelectTab(tab) },
                label = { Text(tab.displayName()) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
private fun TopicMaterialCard(item: DataItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = typeLabel(item.type),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.title ?: contentPreview(item.effectiveContent, item.type, item.title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = contentPreview(item.effectiveContent, item.type, item.title),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TopicAnalysisCard(analysis: TopicAnalysis) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "분석 결과",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatTimestamp(analysis.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                text = analysis.summary,
                style = MaterialTheme.typography.bodyMedium
            )
            analysis.keyPoints.forEach { point ->
                Text(
                    text = "• $point",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun TopicActionCard(
    action: TopicAction,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onShareDraft: () -> Unit,
    onCreateCalendarDraft: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = action.type.displayName(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = action.status.displayName(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            OutlinedTextField(
                value = action.title,
                onValueChange = onTitleChange,
                label = { Text("작업명") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = action.body,
                onValueChange = onBodyChange,
                label = { Text("초안") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (action.type) {
                    TopicActionType.CALENDAR -> {
                        OutlinedButton(
                            onClick = onCreateCalendarDraft,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("캘린더 열기")
                        }
                    }
                    TopicActionType.SHARE_DRAFT,
                    TopicActionType.SUMMARY -> {
                        OutlinedButton(
                            onClick = onShareDraft,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("공유 초안")
                        }
                    }
                    TopicActionType.REMINDER,
                    TopicActionType.TODO -> {
                        OutlinedButton(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("실행 준비 중")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RunningAnalysisCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "AI Agent가 선택한 자료를 분석하고 있어요.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyTopicState(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun SelectionSummaryCard(
    selectedCount: Int,
    hiddenSelectedCount: Int,
    onSelectVisible: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "선택한 데이터 ${selectedCount}개",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (hiddenSelectedCount > 0) {
                        Text(
                            text = "현재 화면 밖 선택 ${hiddenSelectedCount}개 포함",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                TextButton(onClick = onSelectVisible) {
                    Text("보이는 것 선택")
                }
                if (selectedCount > 0) {
                    TextButton(onClick = onClear) {
                        Text("초기화")
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionActionBar(
    selectedCount: Int,
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "선택 ${selectedCount}개",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onCancel) {
                    Text("취소")
                }
                Button(
                    onClick = onNext,
                    enabled = selectedCount > 0
                ) {
                    Text("다음")
                }
            }
        }
    }
}

@Composable
private fun EmptyDataMessage(modifier: Modifier = Modifier) {
    Text(
        text = "아직 수집된 데이터가 없어요.\n공유 메뉴, 클립보드 타일, 스크린샷 가져오기로 시작해보세요.",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.outline,
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
    )
}

@Composable
private fun ItemCard(
    item: DataItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val shouldShowThumbnail = (item.type == DataItemType.IMAGE || item.type == DataItemType.SCREENSHOT) &&
            (item.content.startsWith("content://") || item.content.startsWith("file://"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column {
            if (shouldShowThumbnail) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(144.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "미리보기 준비 중",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = typeLabel(item.type),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(Uri.parse(item.content))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelect() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = typeLabel(item.type),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.title ?: item.effectiveContent,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDelete) {
                        Text("삭제", color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = contentPreview(item.effectiveContent, item.type, item.title),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = sourceLabel(item.source),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    item.mimeType?.let { mime ->
                        Text(
                            text = mime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = formatTimestamp(item.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * 직접 고르기에서 선택한 데이터를 AgentSession에 전달하여 동일한 파이프라인으로 진행.
 * GeminiCluster 대신 사용자가 직접 주제와 데이터를 선택한 경우.
 */
@Composable
private fun AgentSessionWithItemsScreen(
    topicQuery: String,
    selectedItemIds: List<Long>
) {
    val agentViewModel: AgentSessionViewModel = hiltViewModel()

    LaunchedEffect(topicQuery, selectedItemIds) {
        if (topicQuery.isNotBlank() && selectedItemIds.isNotEmpty()) {
            agentViewModel.onIntent(
                AgentSessionIntent.StartWithClusterItems(topicQuery, selectedItemIds)
            )
        }
    }

    AgentSessionScreen(viewModel = agentViewModel)
}

@Composable
private fun AgentSessionPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = AppColors.Blue,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "AI 에이전트",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "AI 찾기 또는 직접 고르기로 시작하세요",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Slate400
        )
    }
}

@Composable
private fun Pill(text: String, bg: Color, color: Color) {
    Box(Modifier.clip(CircleShape).background(bg).padding(horizontal = 7.dp, vertical = 3.dp)) {
        Text(text, color = color, fontSize = 9.sp, maxLines = 1)
    }
}

@Composable
private fun DividerLine(modifier: Modifier = Modifier) {
    Box(modifier.height(1.dp).background(AppColors.Slate200))
}

// ---------------------------------------------------------------------------
// Helper functions
// ---------------------------------------------------------------------------

private fun typeLabel(type: DataItemType): String {
    return when (type) {
        DataItemType.TEXT -> "메모"
        DataItemType.LINK -> "링크"
        DataItemType.IMAGE -> "이미지"
        DataItemType.FILE -> "파일"
        DataItemType.SCREENSHOT -> "스크린샷"
    }
}

private fun sourceLabel(source: String?): String {
    return when (source) {
        "share" -> "공유"
        "clipboard_tile" -> "클립보드"
        "mediastore_screenshot" -> "스크린샷"
        null, "", "Unknown source" -> "출처 없음"
        else -> source
    }
}

private fun contentPreview(content: String, type: DataItemType, title: String?): String {
    if ((type == DataItemType.IMAGE || type == DataItemType.SCREENSHOT) && !title.isNullOrBlank()) {
        return title
    }
    if (content.startsWith("content://") || content.startsWith("file://")) {
        val name = content.substringAfterLast("/")
        if (name.isNotBlank()) return name
    }
    return if (content.length > 120) {
        content.take(120).replace('\n', ' ') + "..."
    } else {
        content.replace('\n', ' ')
    }
}

private fun formatTimestamp(millis: Long): String {
    return try {
        val sdf = SimpleDateFormat("M월 d일 HH:mm", Locale.getDefault())
        sdf.format(Date(millis))
    } catch (e: Exception) {
        ""
    }
}

private fun MainFilter.displayName(): String {
    return when (this) {
        MainFilter.ALL -> "전체"
        MainFilter.TEXT -> "메모"
        MainFilter.LINK -> "링크"
        MainFilter.IMAGE -> "이미지"
        MainFilter.FILE -> "파일"
        MainFilter.SCREENSHOT -> "스크린샷"
    }
}

private fun TopicDetailTab.displayName(): String {
    return when (this) {
        TopicDetailTab.MATERIALS -> "자료"
        TopicDetailTab.ANALYSIS -> "분석"
        TopicDetailTab.ACTIONS -> "작업"
    }
}

private fun TopicActionType.displayName(): String {
    return when (this) {
        TopicActionType.SUMMARY -> "요약"
        TopicActionType.CALENDAR -> "일정"
        TopicActionType.REMINDER -> "알림"
        TopicActionType.SHARE_DRAFT -> "공유"
        TopicActionType.TODO -> "할 일"
    }
}

private fun TopicActionStatus.displayName(): String {
    return when (this) {
        TopicActionStatus.DRAFT -> "초안"
        TopicActionStatus.EDITED -> "수정됨"
        TopicActionStatus.EXECUTED -> "실행됨"
        TopicActionStatus.DISMISSED -> "보류"
    }
}

