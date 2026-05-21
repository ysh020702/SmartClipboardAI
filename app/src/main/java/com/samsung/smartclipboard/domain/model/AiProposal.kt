package com.samsung.smartclipboard.domain.model

data class AiProposal(
    val id: Long = 0L,
    val title: String,
    val description: String,
    val confidence: Float,
    val category: String,
    val itemIds: List<Long>,
    val createdAt: Long
)