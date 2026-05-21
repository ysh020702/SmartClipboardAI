package com.samsung.smartclipboard.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "topic_item_cross_refs",
    primaryKeys = ["topicId", "itemId"],
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DataItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("topicId"),
        Index("itemId")
    ]
)
data class TopicItemCrossRefEntity(
    val topicId: Long,
    val itemId: Long,
    val addedAt: Long,
    val addedBy: String
)
