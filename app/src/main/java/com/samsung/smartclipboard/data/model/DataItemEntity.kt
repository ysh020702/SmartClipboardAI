package com.samsung.smartclipboard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_items")
data class DataItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: String,
    val content: String,
    val title: String? = null,
    val source: String? = null,
    val mimeType: String? = null,
    val createdAt: Long,
    val extractedContent: String? = null
)
