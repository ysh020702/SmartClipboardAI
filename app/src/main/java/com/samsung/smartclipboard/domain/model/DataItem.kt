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
)
