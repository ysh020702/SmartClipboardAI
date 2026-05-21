package com.samsung.smartclipboard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_proposals")
data class AiProposalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String,
    val confidence: Float,
    val category: String,
    val itemIds: String, // JSON array stored as String
    val createdAt: Long
)