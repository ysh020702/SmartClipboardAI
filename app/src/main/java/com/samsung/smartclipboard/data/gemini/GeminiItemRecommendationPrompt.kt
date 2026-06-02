package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.domain.model.CandidateItem
import com.samsung.smartclipboard.domain.model.DataItemType
import com.samsung.smartclipboard.domain.model.RetrievalPlan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * GeminiItemRecommendationAgent에 전달할 프롬프트를 생성하는 유틸.
 *
 * 최대 20개 후보 아이템을 JSON 형태로 포함하여
 * Gemini가 추천 아이템을 선택하고 이유를 생성하도록 지시한다.
 */
internal object GeminiItemRecommendationPrompt {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun build(
        topicQuery: String,
        plan: RetrievalPlan,
        candidates: List<CandidateItem>
    ): String {
        val limitedCandidates = candidates.take(20)
        val candidatesJson = buildCandidatesJson(limitedCandidates)
        val candidateIdList = limitedCandidates.joinToString(", ") { it.item.id.toString() }

        return buildString {
            append("너는 Android 로컬 데이터 기반 아이템 추천 비서다.\n")
            append("사용자의 주제와 검색 계획, 후보 아이템 목록을 보고 추천할 아이템을 선택해라.\n\n")

            append("## 반드시 지킬 규칙\n")
            append("- 응답은 반드시 JSON object 하나만 출력한다.\n")
            append("- markdown 코드 펜스(\\`\\`\\`), 설명문, 주석을 절대 포함하지 마라.\n")
            append("- JSON object 외에 다른 텍스트를 출력하지 마라.\n")
            append("- 아래 후보에 없는 itemId를 절대 반환하지 마라.\n")
            append("- 새 itemId를 생성하지 마라.\n")
            append("- item content를 그대로 복사하지 말고 추천 이유만 요약해라.\n")
            append("- 개인정보, 연락처, 주소, URL 등 민감할 수 있는 값을 불필요하게 재출력하지 마라.\n")
            append("- itemId는 절대 번역하지 마라.\n")
            append("- 추천 이유는 한국어로 작성해라.\n\n")

            append("## 사용 가능한 itemId 목록\n")
            append("$candidateIdList\n\n")

            append("## 출력 JSON schema\n")
            append("{\n")
            append("  \"selectedItemIds\": [1, 2, 3],\n")
            append("  \"itemReasons\": [\n")
            append("    {\n")
            append("      \"itemId\": 1,\n")
            append("      \"score\": 0.92,\n")
            append("      \"reason\": \"주제의 핵심 키워드와 가장 직접적으로 관련됩니다.\"\n")
            append("    }\n")
            append("  ],\n")
            append("  \"recommendationReason\": \"최근 데이터와 주제 관련도를 기준으로 3개를 우선 선택했습니다.\",\n")
            append("  \"suggestedQueries\": [\"추가 검색어1\"]\n")
            append("}\n\n")

            append("## 필드 규칙\n")
            append("- selectedItemIds: 위 목록에 존재하는 itemId만 사용, Long 숫자 배열, 0~5개\n")
            append("- itemReasons: 추천한 각 아이템 별 점수와 이유, 존재하는 itemId만 사용\n")
            append("  - score: 0.0~1.0\n")
            append("  - reason: 1~2문장의 한국어 추천 이유\n")
            append("  - 최대 10개\n")
            append("- recommendationReason: 전체 추천에 대한 한국어 설명 1~3문장\n")
            append("- suggestedQueries: 추가 검색어 제안, 0~5개, 빈 문자열 금지\n\n")

            append("## 사용자 주제\n")
            append("$topicQuery\n\n")

            append("## 검색 계획\n")
            append("키워드: ${plan.keywords.joinToString(", ").ifBlank { "없음" }}\n")
            append("타입 필터: ${plan.typeFilters.joinToString(", ") { it.name }.ifBlank { "없음" }}\n")
            append("날짜 범위: ${plan.dateRangeDays?.let { "${it}일" } ?: "없음"}\n")
            append("최대 결과: ${plan.maxResults}\n\n")

            append("## 후보 아이템 목록 (${limitedCandidates.size}개)\n")
            append(candidatesJson)
        }
    }

    // --- private helpers ---

    private fun buildCandidatesJson(candidates: List<CandidateItem>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        candidates.forEachIndexed { index, candidate ->
            val item = candidate.item
            val comma = if (index < candidates.size - 1) "," else ""
            sb.append("  {\n")
            sb.append("    \"id\": ${item.id},\n")
            sb.append("    \"type\": \"${item.type.name}\",\n")
            sb.append("    \"title\": ${item.title?.let { "\"${escapeJson(it)}\"" } ?: "null"},\n")
            sb.append("    \"source\": ${item.source?.let { "\"${escapeJson(it)}\"" } ?: "null"},\n")
            sb.append("    \"mimeType\": ${item.mimeType?.let { "\"${escapeJson(it)}\"" } ?: "null"},\n")
            sb.append("    \"contentPreview\": \"${escapeJson(contentPreview(item))}\",\n")
            sb.append("    \"createdAt\": \"${formatDate(item.createdAt)}\",\n")
            sb.append("    \"localScore\": ${candidate.relevanceScore},\n")
            sb.append("    \"localReason\": \"${escapeJson(candidate.relevanceReason)}\"\n")
            sb.append("  }$comma\n")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun contentPreview(item: com.samsung.smartclipboard.domain.model.DataItem): String {
        // 이미지/파일 타입은 content가 바이너리성일 수 있으므로 title/source/mimeType 중심
        return when (item.type) {
            DataItemType.IMAGE,
            DataItemType.SCREENSHOT,
            DataItemType.FILE -> {
                listOfNotNull(item.title, item.source, item.mimeType)
                    .joinToString(" / ")
                    .take(300)
            }
            else -> item.effectiveContent.take(300)
        }
    }

    private fun formatDate(epochMillis: Long): String {
        return try {
            dateFormat.format(Date(epochMillis))
        } catch (_: Exception) {
            epochMillis.toString()
        }
    }

    private fun escapeJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
