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

