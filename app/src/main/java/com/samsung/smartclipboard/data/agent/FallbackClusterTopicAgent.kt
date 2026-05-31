package com.samsung.smartclipboard.data.agent

import com.samsung.smartclipboard.domain.agent.ClusterTopicAgent
import com.samsung.smartclipboard.domain.model.DataCluster
import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.SuggestedTopic

class FallbackClusterTopicAgent : ClusterTopicAgent {

    override suspend fun suggestTopics(
        clusters: List<DataCluster>,
        items: List<DataItem>
    ): Result<List<DataCluster>> {
        if (clusters.isEmpty()) return Result.success(emptyList())

        val itemById = items.associateBy { it.id }
        return Result.success(
            clusters.mapNotNull { cluster ->
                val validIds = cluster.itemIds.filter { it in itemById }
                if (validIds.isEmpty()) return@mapNotNull null

                val label = cluster.clusterLabel.ifBlank {
                    val sampleIds = validIds.take(3)
                    sampleIds.joinToString(" · ") { id ->
                        itemById[id]?.title ?: itemById[id]?.content?.take(30) ?: id.toString()
                    }
                }
                val cappedLabel = if (label.length > 40) label.take(40) + "..." else label

                val confidence = when {
                    validIds.size >= 5 -> 0.72f
                    validIds.size >= 2 -> 0.64f
                    else -> 0.52f
                }

                val topics = listOf(
                    SuggestedTopic(
                        suggestedTitle = "${cappedLabel} 정리하기",
                        description = "관련 데이터 ${validIds.size}개를 바탕으로 정리, 요약, 후속 작업을 만들 수 있습니다.",
                        confidence = confidence,
                        reason = "비슷한 키워드와 데이터 유형이 함께 묶였습니다.",
                        relatedClusterId = cluster.clusterId
                    ),
                    SuggestedTopic(
                        suggestedTitle = "${cappedLabel}로 할 일 만들기",
                        description = "선택한 데이터에서 해야 할 일을 추출해 정리합니다.",
                        confidence = confidence - 0.1f,
                        reason = "클러스터의 데이터를 작업 목록으로 변환할 수 있습니다.",
                        relatedClusterId = cluster.clusterId
                    ),
                    SuggestedTopic(
                        suggestedTitle = "${cappedLabel} 공유 초안 만들기",
                        description = "관련 데이터를 바탕으로 공유 가능한 문서 초안을 작성합니다.",
                        confidence = confidence - 0.15f,
                        reason = "클러스터 정보를 다른 사람과 공유하기 좋은 형태로 정리합니다.",
                        relatedClusterId = cluster.clusterId
                    )
                )

                cluster.copy(
                    clusterLabel = cappedLabel,
                    itemIds = validIds,
                    topicCandidates = topics.take(3)
                )
            }.take(10)
        )
    }
}