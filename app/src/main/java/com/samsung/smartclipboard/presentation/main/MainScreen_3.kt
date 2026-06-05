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
