package com.samsung.smartclipboard.data.gemini

import android.util.Log
import com.samsung.smartclipboard.data.retrieval.LocalClusterer
import com.samsung.smartclipboard.domain.ai.GeminiManager
import com.samsung.smartclipboard.domain.model.DataCluster
import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.retrieval.DataClusterer

/**
 * Gemini 기반 클러스터링 구현체.
 *
 * 각 DataItem의 purpose/purposeKeyword를 Gemini에게 전달하여
 * 클러스터 할당(assignments)과 라벨을 받아온다.
 * Gemini 호출 실패 시 LocalClusterer로 폴백한다.
 */
class GeminiClusterer(
    private val geminiManager: GeminiManager,
    private val fallback: LocalClusterer
) : DataClusterer {

    companion object {
        private const val TAG = "GeminiClusterer"
        private const val MAX_ITEMS = 50
    }

    override suspend fun cluster(items: List<DataItem>): List<DataCluster> {
        if (items.isEmpty()) return emptyList()

        // purpose가 있는 아이템이 하나도 없으면 로컬 폴백
        val hasPurpose = items.any { !it.purpose.isNullOrBlank() || !it.purposeKeyword.isNullOrBlank() }
        if (!hasPurpose) {
            Log.w(TAG, "purpose가 있는 아이템이 없어 로컬 클러스터링으로 폴백")
            return fallback.cluster(items)
        }

        val limited = items.take(MAX_ITEMS)

        return try {
            val prompt = GeminiClusterPrompt.build(limited)
            val raw = geminiManager.run(prompt)
            val result = GeminiClusterJsonParser.parse(raw)

            if (result == null) {
                Log.w(TAG, "Gemini 클러스터링 파싱 실패, 로컬 폴백")
                return fallback.cluster(items)
            }

            if (result.assignments.size != limited.size) {
                Log.w(TAG, "Gemini assignments 크기 불일치: expected=${limited.size}, got=${result.assignments.size}, 로컬 폴백")
                return fallback.cluster(items)
            }

            // assignments와 items를 매핑하여 클러스터 구성
            val clusterGroups = mutableMapOf<Int, MutableList<DataItem>>()
            limited.forEachIndexed { index, item ->
                val clusterId = result.assignments.getOrElse(index) { 1 }
                clusterGroups.getOrPut(clusterId) { mutableListOf() }.add(item)
            }

            val clusters = clusterGroups.map { (clusterId, groupItems) ->
                val label = result.clusterLabels[clusterId]
                    ?: groupItems.firstOrNull()?.purpose?.take(15)
                    ?: "데이터 묶음 $clusterId"
                DataCluster(
                    clusterId = "cluster_$clusterId",
                    clusterLabel = label.take(40),
                    itemIds = groupItems.map { it.id },
                    topicCandidates = emptyList(),
                    generatedAt = System.currentTimeMillis()
                )
            }.sortedByDescending { it.itemIds.size }

            // MAX_ITEMS 이후 아이템들을 로컬 클러스터링 결과와 병합
            val remaining = items.drop(MAX_ITEMS)
            val extraClusters = if (remaining.isNotEmpty()) {
                fallback.cluster(remaining)
            } else {
                emptyList()
            }

            clusters + extraClusters
        } catch (e: Exception) {
            Log.e(TAG, "Gemini 클러스터링 실패, 로컬 폴백", e)
            fallback.cluster(items)
        }
    }
}