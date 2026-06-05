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
