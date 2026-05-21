package com.samsung.smartclipboard.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "topics",
    indices = [Index(value = ["title"], unique = true)]
)
data class TopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)
