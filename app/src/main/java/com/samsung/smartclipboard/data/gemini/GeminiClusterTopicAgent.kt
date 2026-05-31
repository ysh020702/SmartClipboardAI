package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.data.agent.FallbackClusterTopicAgent
import com.samsung.smartclipboard.domain.agent.ClusterTopicAgent
import com.samsung.smartclipboard.domain.ai.GeminiManager
import com.samsung.smartclipboard.domain.model.DataCluster
import com.samsung.smartclipboard.domain.model.DataItem

class GeminiClusterTopicAgent(
    private val geminiManager: GeminiManager,
    private val fallbackAgent: ClusterTopicAgent = FallbackClusterTopicAgent()
) : ClusterTopicAgent {

    override suspend fun suggestTopics(
        clusters: List<DataCluster>,
        items: List<DataItem>
    ): Result<List<DataCluster>> {
        if (clusters.isEmpty()) return Result.success(emptyList())
        if (items.isEmpty()) return fallbackAgent.suggestTopics(clusters, items)

        return try {
            val clustersForPrompt = clusters.take(10)
            val prompt = GeminiClusterTopicPrompt.build(clustersForPrompt, items)
            val rawResponse = geminiManager.run(prompt)

            val parseResult = GeminiClusterTopicJsonParser.parseClusterTopics(rawResponse, clustersForPrompt)
            parseResult.fold(
                onSuccess = { result ->
                    if (validateClusters(result, clustersForPrompt)) Result.success(result)
                    else fallbackAgent.suggestTopics(clusters, items)
                },
                onFailure = { fallbackAgent.suggestTopics(clusters, items) }
            )
        } catch (e: Exception) {
            fallbackAgent.suggestTopics(clusters, items)
        }
    }

    private fun validateClusters(
        result: List<DataCluster>,
        originalClusters: List<DataCluster>
    ): Boolean {
        val originalIds = originalClusters.map { it.clusterId }.toSet()
        return result.all { cluster ->
            cluster.clusterId in originalIds &&
            cluster.itemIds.isNotEmpty() &&
            cluster.topicCandidates.all { topic ->
                topic.suggestedTitle.isNotBlank() &&
                topic.confidence in 0.0f..1.0f &&
                (topic.relatedClusterId == null || topic.relatedClusterId == cluster.clusterId)
            }
        }
    }
}