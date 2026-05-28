package com.samsung.smartclipboard.domain.repository

import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.TopicAction
import com.samsung.smartclipboard.domain.model.Topic
import com.samsung.smartclipboard.domain.model.TopicAnalysis
import kotlinx.coroutines.flow.Flow

interface DataRepository {
    fun observeItems(): Flow<List<DataItem>>
    fun observeTopics(): Flow<List<Topic>>
    fun observeTopicItems(topicId: Long): Flow<List<DataItem>>
    fun observeTopicAnalysis(topicId: Long): Flow<List<TopicAnalysis>>
    fun observeTopicActions(topicId: Long): Flow<List<TopicAction>>
    suspend fun addText(text: String, source: String? = null)
    suspend fun addLink(url: String, title: String? = null, source: String? = null)
    suspend fun addMedia(uri: String, mimeType: String? = null, source: String? = null)
    suspend fun addScreenshot(
        uri: String,
        title: String? = null,
        mimeType: String? = null,
        source: String? = null,
        createdAt: Long = System.currentTimeMillis()
    )
    suspend fun updateScreenshotTimestamp(uri: String, createdAt: Long)
    suspend fun deleteItem(id: Long)
    suspend fun clearAll()
    suspend fun addItemsToTopic(title: String, itemIds: List<Long>, addedBy: String = "USER"): Long
    suspend fun runTopicAnalysis(topicId: Long): Boolean
    suspend fun updateTopicActionDraft(actionId: Long, title: String, body: String)
}
