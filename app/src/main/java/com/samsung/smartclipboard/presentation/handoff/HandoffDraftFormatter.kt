package com.samsung.smartclipboard.presentation.handoff

import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.DataItemType

data class HandoffDraft(
    val title: String,
    val body: String,
    val hasPossibleDateTime: Boolean,
    val calendarTitle: String?,
    val calendarDescription: String?
)

object HandoffDraftFormatter {

    fun hasPossibleDateTime(text: String): Boolean {
        return detectPossibleDateTime(text)
    }

    fun formatDraft(items: List<DataItem>): HandoffDraft {
        if (items.isEmpty()) {
            return HandoffDraft(
                title = "Collected items",
                body = "",
                hasPossibleDateTime = false,
                calendarTitle = null,
                calendarDescription = null
            )
        }

        val title = if (items.size == 1) "Collected item" else "Collected items (${items.size})"
        val sb = StringBuilder()
        sb.appendLine("Smart Clipboard AI Draft")
        sb.appendLine()
        sb.appendLine("Items: ${items.size}")
        sb.appendLine()

        for (item in items) {
            val typeLabel = when (item.type) {
                DataItemType.TEXT -> "Text"
                DataItemType.LINK -> "Link"
                DataItemType.IMAGE -> "Image"
                DataItemType.FILE -> "File"
                DataItemType.SCREENSHOT -> "Screenshot"
            }
            sb.appendLine("[$typeLabel] ${item.title ?: contentPreview(item)}")
            if (item.type == DataItemType.LINK) {
                sb.appendLine(item.content)
            } else if (item.type != DataItemType.IMAGE && item.type != DataItemType.SCREENSHOT) {
                sb.appendLine(contentPreview(item))
            }
            val sourceLabel = when (item.source) {
                "share" -> "Shared"
                "clipboard_tile" -> "Clipboard tile"
                "mediastore_screenshot" -> "Screenshot"
                null, "" -> null
                else -> item.source
            }
            if (sourceLabel != null) {
                sb.appendLine("Source: $sourceLabel")
            }
            sb.appendLine()
        }

        val body = sb.toString().trimEnd()
        val hasDateTime = detectPossibleDateTime(body)
        val calendarTitle = items.firstNotNullOfOrNull { it.title } ?: title
        val calendarDesc = body

        return HandoffDraft(
            title = title,
            body = body,
            hasPossibleDateTime = hasDateTime,
            calendarTitle = calendarTitle,
            calendarDescription = calendarDesc
        )
    }

    private fun contentPreview(item: DataItem): String {
        val content = item.content
        if (content.startsWith("content://") || content.startsWith("file://")) {
            val name = content.substringAfterLast("/")
            if (name.isNotBlank()) return name
        }
        return if (content.length > 500) {
            content.take(500).replace('\n', ' ') + "..."
        } else {
            content.replace('\n', ' ')
        }
    }

    private fun detectPossibleDateTime(text: String): Boolean {
        val patterns = listOf(
            Regex("\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}"),
            Regex("\\d{1,2}[-/]\\d{1,2}"),
            Regex("\\d{1,2}월\\s*\\d{1,2}일"),
            Regex("\\d{1,2}:\\d{2}"),
            Regex("[AP]M", RegexOption.IGNORE_CASE),
            Regex("오전|오후"),
            Regex("\\d{1,2}시")
        )
        return patterns.any { it.containsMatchIn(text) }
    }
}