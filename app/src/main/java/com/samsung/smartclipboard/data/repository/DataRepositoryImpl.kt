package com.samsung.smartclipboard.data.repository

import com.samsung.smartclipboard.data.model.AiProposalEntity
import com.samsung.smartclipboard.data.model.DataItemEntity
import com.samsung.smartclipboard.data.model.TopicActionEntity
import com.samsung.smartclipboard.data.model.TopicAnalysisEntity
import com.samsung.smartclipboard.data.model.TopicEntity
import com.samsung.smartclipboard.data.model.TopicItemCrossRefEntity
import com.samsung.smartclipboard.data.source.local.AiProposalDao
import com.samsung.smartclipboard.data.source.local.DataItemDao
import com.samsung.smartclipboard.data.source.local.TopicDao
import com.samsung.smartclipboard.data.source.local.TopicSummaryRow
import com.samsung.smartclipboard.domain.ai.AiProposalGenerator
import com.samsung.smartclipboard.domain.model.AiProposal
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
    private val aiProposalDao: AiProposalDao,
    private val topicDao: TopicDao,
    private val aiProposalGenerator: AiProposalGenerator
) : DataRepository {

    override fun observeItems(): Flow<List<DataItem>> {
        return dataItemDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeProposals(): Flow<List<AiProposal>> {
        return aiProposalDao.observeAll().map { entities ->
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
        dataItemDao.insert(entity)
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
        dataItemDao.insert(entity)
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
        dataItemDao.insert(entity)
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
        aiProposalDao.clearAll()
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

    override suspend fun runTopicAnalysis(topicId: Long) {
        val items = topicDao.observeItemsForTopic(topicId).first().map { it.toDomain() }
        if (items.isEmpty()) return

        val now = System.currentTimeMillis()
        val summary = buildTopicSummary(items)
        val keyPoints = buildTopicKeyPoints(items)
        val analysisId = topicDao.insertAnalysis(
            TopicAnalysisEntity(
                topicId = topicId,
                summary = summary,
                keyPoints = keyPoints.joinToString("\n"),
                sourceItemIds = items.map { it.id }.joinToString(","),
                createdAt = now
            )
        )

        val actions = buildTopicActions(
            topicId = topicId,
            analysisResultId = analysisId,
            items = items,
            summary = summary,
            createdAt = now
        )
        topicDao.insertActions(actions)
        topicDao.updateTopicTimestamp(topicId, now)
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

    override suspend fun generateProposals() {
        val items = dataItemDao.observeAll().first().map { it.toDomain() }
        val proposals = aiProposalGenerator.generateProposals(items)

        if (proposals.isNotEmpty()) {
            aiProposalDao.clearAll() // Replace old proposals
            val now = System.currentTimeMillis()
            for (proposal in proposals) {
                aiProposalDao.insert(
                    AiProposalEntity(
                        title = proposal.title,
                        description = proposal.description,
                        confidence = proposal.confidence,
                        category = proposal.category,
                        itemIds = proposal.itemIds.joinToString(","),
                        createdAt = now
                    )
                )
            }
        }
    }

    override suspend fun clearProposals() {
        aiProposalDao.clearAll()
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
            createdAt = createdAt
        )
    }

    private fun AiProposalEntity.toDomain(): AiProposal {
        val ids = try {
            itemIds.split(",").mapNotNull { it.trim().toLongOrNull() }
        } catch (e: Exception) {
            emptyList()
        }
        return AiProposal(
            id = id,
            title = title,
            description = description,
            confidence = confidence,
            category = category,
            itemIds = ids,
            createdAt = createdAt
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

    private fun buildTopicSummary(items: List<DataItem>): String {
        val typeCounts = DataItemType.entries
            .associateWith { type -> items.count { it.type == type } }
            .filterValues { it > 0 }
            .map { (type, count) -> "${type.name.lowercase()}: $count" }
            .joinToString(", ")
        val preview = items.take(3).joinToString("\n") { "- ${it.title ?: it.content.take(80)}" }
        return "선택된 ${items.size}개 데이터가 이 주제의 자료로 묶였습니다.\n$typeCounts\n\n대표 자료\n$preview"
    }

    private fun buildTopicKeyPoints(items: List<DataItem>): List<String> {
        val points = mutableListOf<String>()
        val links = items.count { it.type == DataItemType.LINK }
        val screenshots = items.count { it.type == DataItemType.SCREENSHOT }
        val texts = items.count { it.type == DataItemType.TEXT }
        if (links > 0) points += "링크 ${links}개는 리서치/참고 자료 후보입니다."
        if (screenshots > 0) points += "스크린샷 ${screenshots}개는 OCR과 이미지 분석이 필요합니다."
        if (texts > 0) points += "메모 ${texts}개는 요약과 할 일 추출에 사용할 수 있습니다."
        if (points.isEmpty()) points += "자료를 요약하고 다음 작업 후보를 만들 수 있습니다."
        return points
    }

    private fun buildTopicActions(
        topicId: Long,
        analysisResultId: Long,
        items: List<DataItem>,
        summary: String,
        createdAt: Long
    ): List<TopicActionEntity> {
        val actions = mutableListOf<TopicActionEntity>()
        val hasDateCandidate = items.any { item ->
            item.content.contains(Regex("\\d{1,2}:\\d{2}|\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}|AM|PM|오전|오후", RegexOption.IGNORE_CASE))
        }

        actions += TopicActionEntity(
            topicId = topicId,
            analysisResultId = analysisResultId,
            type = TopicActionType.SUMMARY.name,
            title = "요약 문서 만들기",
            body = summary,
            status = TopicActionStatus.DRAFT.name,
            editablePayload = null,
            createdAt = createdAt,
            updatedAt = createdAt
        )

        if (hasDateCandidate) {
            actions += TopicActionEntity(
                topicId = topicId,
                analysisResultId = analysisResultId,
                type = TopicActionType.CALENDAR.name,
                title = "캘린더 일정 초안",
                body = "선택한 자료에서 날짜/시간 후보가 발견되었습니다. 제목과 설명을 검토한 뒤 캘린더에 추가하세요.",
                status = TopicActionStatus.DRAFT.name,
                editablePayload = null,
                createdAt = createdAt,
                updatedAt = createdAt
            )
        }

        actions += TopicActionEntity(
            topicId = topicId,
            analysisResultId = analysisResultId,
            type = TopicActionType.TODO.name,
            title = "다음 할 일 정리",
            body = "자료를 검토하고 실행할 작업을 확정하세요.",
            status = TopicActionStatus.DRAFT.name,
            editablePayload = null,
            createdAt = createdAt,
            updatedAt = createdAt
        )

        return actions
    }
}
