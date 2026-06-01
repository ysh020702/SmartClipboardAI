package com.samsung.smartclipboard.data.repository

import android.util.Log
import com.samsung.smartclipboard.data.model.DataItemEntity
import com.samsung.smartclipboard.data.model.TopicActionEntity
import com.samsung.smartclipboard.data.model.TopicAnalysisEntity
import com.samsung.smartclipboard.data.model.TopicEntity
import com.samsung.smartclipboard.data.model.TopicItemCrossRefEntity
import com.samsung.smartclipboard.data.source.local.DataItemDao
import com.samsung.smartclipboard.data.source.local.TopicDao
import com.samsung.smartclipboard.data.source.local.TopicSummaryRow
import com.samsung.smartclipboard.domain.ai.SourceExtractor
import com.samsung.smartclipboard.domain.ai.TopicAgent
import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.DataItemType
import com.samsung.smartclipboard.domain.model.Topic
import com.samsung.smartclipboard.domain.model.TopicAction
import com.samsung.smartclipboard.domain.model.TopicActionStatus
import com.samsung.smartclipboard.domain.model.TopicActionType
import com.samsung.smartclipboard.domain.model.TopicAnalysis
import com.samsung.smartclipboard.domain.repository.DataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataRepositoryImpl @Inject constructor(
    private val dataItemDao: DataItemDao,
    private val topicDao: TopicDao,
    private val topicAgent: TopicAgent,
    private val sourceExtractor: SourceExtractor
) : DataRepository {

    override fun observeItems(): Flow<List<DataItem>> {
        return dataItemDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeTopics(): Flow<List<Topic>> {
        return topicDao.observeTopicSummaries().map { rows ->
            rows.map { it.toDomain() }
        }
    }

    override fun observeTopicItems(topicId: Long): Flow<List<DataItem>> {
        return topicDao.observeItemsForTopic(topicId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeTopicAnalysis(topicId: Long): Flow<List<TopicAnalysis>> {
        return topicDao.observeAnalysisForTopic(topicId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeTopicActions(topicId: Long): Flow<List<TopicAction>> {
        return topicDao.observeActionsForTopic(topicId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addText(text: String, source: String?) {
        val entity = DataItemEntity(
            type = DataItemType.TEXT.name,
            content = text,
            source = source,
            createdAt = System.currentTimeMillis()
        )
        dataItemDao.insert(entity)
    }

    override suspend fun addLink(url: String, title: String?, source: String?) {
        val entity = DataItemEntity(
            type = DataItemType.LINK.name,
            content = url,
            title = title,
            source = source,
            createdAt = System.currentTimeMillis()
        )
        val id = dataItemDao.insert(entity)

        try {
            val extracted = sourceExtractor.extractFromUrl(url)
            if (extracted.isNotBlank()) {
                dataItemDao.updateExtractedContent(id, extracted)
            }
        } catch (e: Exception) {
            Log.w("DataRepository", "URL 내용 추출 실패: $url", e)
        }
    }

    override suspend fun addMedia(uri: String, mimeType: String?, source: String?) {
        val type = if (mimeType?.startsWith("image/") == true) {
            DataItemType.IMAGE
        } else {
            DataItemType.FILE
        }
        val entity = DataItemEntity(
            type = type.name,
            content = uri,
            mimeType = mimeType,
            source = source,
            createdAt = System.currentTimeMillis()
        )
        val id = dataItemDao.insert(entity)

        if (type == DataItemType.IMAGE) {
            try {
                val extracted = sourceExtractor.extractFromOcr(uri)
                if (extracted.isNotBlank()) {
                    dataItemDao.updateExtractedContent(id, extracted)
                }
            } catch (e: Exception) {
                Log.w("DataRepository", "OCR 추출 실패 (media): $uri", e)
            }
        }
    }

    override suspend fun addScreenshot(
        uri: String,
        title: String?,
        mimeType: String?,
        source: String?,
        createdAt: Long
    ) {
        val entity = DataItemEntity(
            type = DataItemType.SCREENSHOT.name,
            content = uri,
            title = title,
            mimeType = mimeType,
            source = source,
            createdAt = createdAt
        )
        val id = dataItemDao.insert(entity)

        try {
            val extracted = sourceExtractor.extractFromOcr(uri)
            if (extracted.isNotBlank()) {
                dataItemDao.updateExtractedContent(id, extracted)
            }
        } catch (e: Exception) {
            Log.w("DataRepository", "OCR 추출 실패 (screenshot): $uri", e)
        }
    }

    override suspend fun updateScreenshotTimestamp(uri: String, createdAt: Long) {
        dataItemDao.updateCreatedAtByContentAndType(
            content = uri,
            type = DataItemType.SCREENSHOT.name,
            createdAt = createdAt
        )
    }

    override suspend fun deleteItem(id: Long) {
        dataItemDao.deleteById(id)
    }

    override suspend fun clearAll() {
        dataItemDao.clearAll()
    }

    override suspend fun addItemsToTopic(title: String, itemIds: List<Long>, addedBy: String): Long {
        val normalizedTitle = title.trim()
        require(normalizedTitle.isNotBlank()) { "Topic title must not be blank" }
        val now = System.currentTimeMillis()
        val topicId = topicDao.findTopicIdByTitle(normalizedTitle)
            ?: topicDao.insertTopic(
                TopicEntity(
                    title = normalizedTitle,
                    createdAt = now,
                    updatedAt = now
                )
            )

        val refs = itemIds.distinct().map { itemId ->
            TopicItemCrossRefEntity(
                topicId = topicId,
                itemId = itemId,
                addedAt = now,
                addedBy = addedBy
            )
        }
        if (refs.isNotEmpty()) {
            topicDao.insertTopicItemRefs(refs)
        }
        topicDao.updateTopicTimestamp(topicId, now)
        return topicId
    }

    override suspend fun runTopicAnalysis(topicId: Long): Boolean {
        val items = topicDao.observeItemsForTopic(topicId).first().map { it.toDomain() }
        if (items.isEmpty()) return false

        val topicRow = topicDao.getTopicById(topicId) ?: return false
        val topic = Topic(
            id = topicRow.id,
            title = topicRow.title,
            itemCount = items.size,
            createdAt = topicRow.createdAt,
            updatedAt = topicRow.updatedAt
        )

        val now = System.currentTimeMillis()

        val result = topicAgent.analyze(topic, items)

        var success = false

        result.onSuccess { agentResult ->
            val analysisId = topicDao.insertAnalysis(
                TopicAnalysisEntity(
                    topicId = topicId,
                    summary = agentResult.summary,
                    keyPoints = agentResult.keyPoints.joinToString("\n"),
                    sourceItemIds = agentResult.sourceItemIds.joinToString(","),
                    createdAt = now
                )
            )

            val actionEntities = agentResult.actions.map { draft ->
                TopicActionEntity(
                    topicId = topicId,
                    analysisResultId = analysisId,
                    type = draft.type.name,
                    title = draft.title,
                    body = draft.body,
                    status = TopicActionStatus.DRAFT.name,
                    editablePayload = draft.payload,
                    createdAt = now,
                    updatedAt = now
                )
            }
            if (actionEntities.isNotEmpty()) {
                topicDao.insertActions(actionEntities)
            }
            success = true
        }

        result.onFailure { exception ->
            android.util.Log.e("DataRepository", "TopicAgent 분석 실패: topicId=$topicId", exception)
        }

        topicDao.updateTopicTimestamp(topicId, now)
        return success
    }

    override suspend fun updateTopicActionDraft(actionId: Long, title: String, body: String) {
        topicDao.updateActionDraft(
            actionId = actionId,
            title = title,
            body = body,
            status = TopicActionStatus.EDITED.name,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun DataItemEntity.toDomain(): DataItem {
        val resolvedType = try {
            DataItemType.valueOf(type)
        } catch (e: IllegalArgumentException) {
            DataItemType.TEXT
        }
        return DataItem(
            id = id,
            type = resolvedType,
            content = content,
            title = title,
            source = source,
            mimeType = mimeType,
            createdAt = createdAt,
            extractedContent = extractedContent
        )
    }

    private fun TopicSummaryRow.toDomain(): Topic {
        return Topic(
            id = id,
            title = title,
            itemCount = itemCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun TopicAnalysisEntity.toDomain(): TopicAnalysis {
        return TopicAnalysis(
            id = id,
            topicId = topicId,
            summary = summary,
            keyPoints = keyPoints.lines().filter { it.isNotBlank() },
            sourceItemIds = sourceItemIds.split(",").mapNotNull { it.trim().toLongOrNull() },
            createdAt = createdAt
        )
    }

    private fun TopicActionEntity.toDomain(): TopicAction {
        return TopicAction(
            id = id,
            topicId = topicId,
            analysisResultId = analysisResultId,
            type = runCatching { TopicActionType.valueOf(type) }.getOrDefault(TopicActionType.TODO),
            title = title,
            body = body,
            status = runCatching { TopicActionStatus.valueOf(status) }.getOrDefault(TopicActionStatus.DRAFT),
            editablePayload = editablePayload,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

}
