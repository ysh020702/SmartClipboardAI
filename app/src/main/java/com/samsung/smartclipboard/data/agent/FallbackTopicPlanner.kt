package com.samsung.smartclipboard.data.agent

import com.samsung.smartclipboard.domain.agent.TopicPlanner
import com.samsung.smartclipboard.domain.model.DataItemType
import com.samsung.smartclipboard.domain.model.RetrievalPlan

/**
 * LLM 호출 없이 topicQuery에서 RetrievalPlan을 생성하는 Fallback 구현체.
 *
 * GeminiTopicPlanner가 실패했을 때도 M5 UI 플로우가 계속 진행될 수 있게 한다.
 */
class FallbackTopicPlanner : TopicPlanner {

    private val koreanParticles = setOf(
        "은", "는", "이", "가", "을", "를",
        "에", "에서", "으로", "로", "과", "와",
        "도", "만", "의"
    )

    override suspend fun plan(topicQuery: String): Result<RetrievalPlan> {
        val trimmed = topicQuery.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("주제가 비어 있습니다"))
        }

        val keywords = extractKeywords(trimmed)
        val typeFilters = inferTypeFilters(trimmed)
        val dateRangeDays = inferDateRangeDays(trimmed)

        return Result.success(
            RetrievalPlan(
                keywords = keywords,
                typeFilters = typeFilters,
                dateRangeDays = dateRangeDays,
                maxResults = 20
            )
        )
    }

    // --- private helpers ---

    /**
     * topicQuery에서 키워드를 추출한다.
     * - 구분자로 토큰화
     * - 한국어 조사 제거
     * - 길이 2 이상만 유지
     * - distinct
     * - 최대 8개
     * - 토큰이 없거나 쿼리가 짧으면 원문을 키워드로 사용
     */
    private fun extractKeywords(topicQuery: String): List<String> {
        val rawTokens = topicQuery
            .split(Regex("[\\s,/.\n\t()\\[\\]{}]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val cleaned = rawTokens.mapNotNull { token ->
            val stripped = koreanParticles.fold(token) { acc, particle ->
                if (acc.endsWith(particle) && acc.length > particle.length) {
                    acc.dropLast(particle.length)
                } else {
                    acc
                }
            }
            stripped.trim().takeIf { it.length >= 2 }
        }

        val distinct = cleaned.distinct()
        val result = if (distinct.isEmpty()) {
            listOf(topicQuery.trim())
        } else {
            distinct
        }
        return result.take(8)
    }

    /**
     * topicQuery의 키워드 기반으로 typeFilters를 추론한다.
     */
    private fun inferTypeFilters(topicQuery: String): List<DataItemType> {
        val lower = topicQuery.lowercase()
        val types = mutableSetOf<DataItemType>()

        if (lower.contains("링크") || lower.contains("url") || lower.contains("link") ||
            lower.contains("사이트") || lower.contains("웹") || lower.contains("주소")
        ) {
            types.add(DataItemType.LINK)
        }

        if (lower.contains("사진") || lower.contains("이미지") || lower.contains("image") ||
            lower.contains("picture") || lower.contains("photo")
        ) {
            types.add(DataItemType.IMAGE)
        }

        if (lower.contains("스크린샷") || lower.contains("캡처") || lower.contains("캡쳐") ||
            lower.contains("screenshot")
        ) {
            types.add(DataItemType.SCREENSHOT)
        }

        if (lower.contains("파일") || lower.contains("pdf") || lower.contains("문서") ||
            lower.contains("첨부") || lower.contains("file") || lower.contains("document")
        ) {
            types.add(DataItemType.FILE)
        }

        if (lower.contains("메모") || lower.contains("텍스트") || lower.contains("글") ||
            lower.contains("내용") || lower.contains("text") || lower.contains("memo") ||
            lower.contains("note")
        ) {
            types.add(DataItemType.TEXT)
        }

        return types.toList()
    }

    /**
     * topicQuery에서 시간 범위를 추론한다.
     * - 오늘 → 1
     * - 어제 → 2
     * - 이번주/이번 주 → 7
     * - 지난주/지난 주 → 14
     * - 이번달/이번 달 → 30
     * - 지난달/지난 달 → 60
     * - 최근 → 30
     * - 없으면 null
     */
    private fun inferDateRangeDays(topicQuery: String): Int? {
        val lower = topicQuery.lowercase()

        return when {
            lower.contains("오늘") || lower.contains("today") -> 1
            lower.contains("어제") || lower.contains("yesterday") -> 2
            lower.contains("이번주") || lower.contains("이번 주") ||
                lower.contains("this week") -> 7
            lower.contains("지난주") || lower.contains("지난 주") ||
                lower.contains("last week") -> 14
            lower.contains("이번달") || lower.contains("이번 달") ||
                lower.contains("this month") -> 30
            lower.contains("지난달") || lower.contains("지난 달") ||
                lower.contains("last month") -> 60
            lower.contains("최근") || lower.contains("recent") -> 30
            else -> null
        }
    }
}