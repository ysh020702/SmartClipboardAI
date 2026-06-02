package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.domain.model.DataCluster
import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.DataItemType
import com.samsung.smartclipboard.domain.model.SuggestedTopic
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object GeminiClusterTopicPrompt {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun build(clusters: List<DataCluster>, items: List<DataItem>): String {
        val itemById = items.associateBy { it.id }
        val clustersJson = buildClustersJson(clusters.take(10), itemById)

        return buildString {
            append("너는 Android 앱에서 사용자 데이터를 분석하는 비서다.\n")
            append("주어진 클러스터는 사용자의 수집 데이터를 자동으로 묶은 그룹이다.\n")
            append("각 클러스터에 대해, 사용자가 AI 에이전트에게 시킬 만한 자연어 추천 주제를 만들어라.\n\n")

            append("## 반드시 지킬 규칙\n")
            append("- 응답은 반드시 JSON object 하나만 출력한다.\n")
            append("- markdown 코드 펜스, 설명문, 주석을 절대 포함하지 마라.\n")
            append("- 새 clusterId를 생성하지 마라. 입력된 clusterId만 사용.\n")
            append("- 새 itemId를 생성하지 마라.\n")
            append("- 추천 주제는 사용자가 그대로 눌러서 AI 에이전트에 입력할 자연어 문장이어야 한다.\n")
            append("- 한국어로 생성해라.\n")
            append("- 개인정보, URL, 주소, 연락처 등 민감한 값을 불필요하게 그대로 재출력하지 마라.\n\n")

            append("## 출력 JSON schema\n")
            append("{\n")
            append("  \"clusters\": [\n")
            append("    {\n")
            append("      \"clusterId\": \"cluster_0_12\",\n")
            append("      \"clusterLabel\": \"여행 일정 · 항공권\",\n")
            append("      \"topicCandidates\": [\n")
            append("        {\n")
            append("          \"suggestedTitle\": \"여행 일정과 항공권 정보를 정리해줘\",\n")
            append("          \"description\": \"관련 캡처와 링크를 바탕으로 이동 일정을 정리합니다.\",\n")
            append("          \"confidence\": 0.82,\n")
            append("          \"reason\": \"여행 일정과 예약 정보가 같은 클러스터에 모여 있습니다.\"\n")
            append("        }\n")
            append("      ]\n")
            append("    }\n")
            append("  ]\n")
            append("}\n\n")

            append("## 필드 규칙\n")
            append("- clusters: 입력 클러스터 중 일부 또는 전체, 최대 10개\n")
            append("- clusterId: 입력된 clusterId 중 하나만 사용\n")
            append("- clusterLabel: 한국어 짧은 라벨 (2~20자 권장)\n")
            append("- topicCandidates: 1~3개\n")
            append("- suggestedTitle: 한국어 자연어 명령형, 8~60자\n")
            append("- description: 1~2문장\n")
            append("- confidence: 0.0~1.0\n")
            append("- reason: 1문장 이유\n\n")

            append("## 클러스터 목록\n")
            append(clustersJson)
        }
    }

    private fun buildClustersJson(clusters: List<DataCluster>, itemById: Map<Long, DataItem>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        clusters.forEachIndexed { ci, cluster ->
            val comma = if (ci < clusters.size - 1) "," else ""
            sb.append("  {\n")
            sb.append("    \"clusterId\": \"${escapeJson(cluster.clusterId)}\",\n")
            sb.append("    \"clusterLabel\": \"${escapeJson(cluster.clusterLabel)}\",\n")
            sb.append("    \"itemCount\": ${cluster.itemIds.size},\n")
            sb.append("    \"items\": [\n")
            cluster.itemIds.take(8).forEachIndexed { ii, id ->
                val item = itemById[id]
                val icomma = if (ii < minOf(cluster.itemIds.size, 8) - 1) "," else ""
                if (item != null) {
                    sb.append("      {\n")
                    sb.append("        \"id\": $id,\n")
                    sb.append("        \"type\": \"${item.type.name}\",\n")
                    sb.append("        \"title\": ${item.title?.let { "\"${escapeJson(it)}\"" } ?: "null"},\n")
                    sb.append("        \"source\": ${item.source?.let { "\"${escapeJson(it)}\"" } ?: "null"},\n")
                    sb.append("        \"contentPreview\": \"${escapeJson(contentPreview(item))}\",\n")
                    sb.append("        \"createdAt\": \"${formatDate(item.createdAt)}\"\n")
                    sb.append("      }$icomma\n")
                }
            }
            sb.append("    ]\n")
            sb.append("  }$comma\n")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun contentPreview(item: DataItem): String {
        return when (item.type) {
            DataItemType.FILE ->
                listOfNotNull(item.title, item.source, item.mimeType).joinToString(" / ").take(1000)
            else -> item.effectiveContent.take(1000)
        }
    }

    private fun formatDate(millis: Long): String =
        try { dateFormat.format(Date(millis)) } catch (_: Exception) { millis.toString() }

    private fun escapeJson(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
}
