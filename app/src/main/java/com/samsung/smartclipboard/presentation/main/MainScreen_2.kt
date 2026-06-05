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
