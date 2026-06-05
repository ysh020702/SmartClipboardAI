package com.samsung.smartclipboard.data.gemini

import org.json.JSONObject

/**
 * Gemini 클러스터링 응답 JSON을 파싱하는 유틸.
 *
 * 예상 입력:
 * ```json
 * {
 *   "assignments": [1, 2, 1, 3, 2],
 *   "clusters": {
 *     "1": "회의 준비",
 *     "2": "여행 계획",
 *     "3": "쇼핑 목록"
 *   }
 * }
 * ```
 */
object GeminiClusterJsonParser {

    data class ClusterResult(
        val assignments: List<Int>,
        val clusterLabels: Map<Int, String>
    )

    fun parse(raw: String): ClusterResult? {
        return try {
            val jsonStr = extractJson(raw) ?: return null
            val json = JSONObject(jsonStr)

            val assignmentsArray = json.getJSONArray("assignments")
            val assignments = mutableListOf<Int>()
            for (i in 0 until assignmentsArray.length()) {
                assignments.add(assignmentsArray.getInt(i))
            }

            val clustersObj = json.getJSONObject("clusters")
            val clusterLabels = mutableMapOf<Int, String>()
            val keys = clustersObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                key.toIntOrNull()?.let { clusterId ->
                    clusterLabels[clusterId] = clustersObj.getString(key)
                }
            }

            if (assignments.isEmpty()) null
            else ClusterResult(assignments, clusterLabels)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gemini 응답에서 ```json ... ``` 블록을 추출.
     * 블록이 없으면 전체 텍스트를 JSON으로 간주.
     */
    private fun extractJson(text: String): String? {
        val regex = Regex("```json\\s*\\n?([\\s\\S]*?)\\n?```")
        val match = regex.find(text)
        if (match != null) {
            return match.groupValues[1].trim()
        }
        // 블록 없이 순수 JSON인지 확인
        val trimmed = text.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed
        }
        return null
    }
}