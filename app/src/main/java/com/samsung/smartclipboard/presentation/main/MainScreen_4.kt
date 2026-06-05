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
