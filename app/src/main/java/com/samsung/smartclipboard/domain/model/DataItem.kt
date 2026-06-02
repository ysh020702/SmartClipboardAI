package com.samsung.smartclipboard.domain.model

data class DataItem(
    val id: Long,
    val type: DataItemType,
    val content: String,
    val title: String? = null,
    val source: String? = null,
    val mimeType: String? = null,
    val createdAt: Long,
    val extractedContent: String? = null
) {
    /** type이 TEXT면 content를, 아니면 extractedContent(없으면 content fallback)를 반환 */
    val effectiveContent: String
        get() = if (type == DataItemType.TEXT) content else (extractedContent ?: content)
}
