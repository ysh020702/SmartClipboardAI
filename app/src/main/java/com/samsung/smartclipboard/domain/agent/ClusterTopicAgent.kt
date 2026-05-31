package com.samsung.smartclipboard.domain.agent

import com.samsung.smartclipboard.domain.model.DataCluster
import com.samsung.smartclipboard.domain.model.DataItem

/**
 * DataCluster 목록에 대해 추천 주제(SuggestedTopic)를 생성하는 인터페이스.
 *
 * LLM은 클러스터 라벨링과 추천 주제 생성에만 사용되며,
 * 클러스터 ID와 itemIds는 원본 데이터를 유지해야 한다.
 */
interface ClusterTopicAgent {

    /**
     * @param clusters 로컬 알고리즘으로 생성된 DataCluster 목록
     * @param items 전체 DataItem 목록 (itemIds 검증 및 context 제공)
     * @return topicCandidates가 채워진 DataCluster 목록. clusterId와 itemIds는 원본 유지.
     */
    suspend fun suggestTopics(
        clusters: List<DataCluster>,
        items: List<DataItem>
    ): Result<List<DataCluster>>
}