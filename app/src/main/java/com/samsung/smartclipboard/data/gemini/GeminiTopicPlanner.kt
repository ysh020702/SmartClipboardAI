package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.data.agent.FallbackTopicPlanner
import com.samsung.smartclipboard.domain.agent.TopicPlanner
import com.samsung.smartclipboard.domain.ai.GeminiManager
import com.samsung.smartclipboard.domain.model.RetrievalPlan
import com.samsung.smartclipboard.util.AgentTraceLogger

/**
 * Gemini LLM을 통해 사용자 주제를 RetrievalPlan으로 변환하는 TopicPlanner 구현체.
 *
 * Gemini 호출 실패 또는 JSON 파싱 실패 시 FallbackTopicPlanner로 fallback한다.
 */
class GeminiTopicPlanner(
    private val geminiManager: GeminiManager,
    private val fallbackTopicPlanner: TopicPlanner = FallbackTopicPlanner()
) : TopicPlanner {

    override suspend fun plan(topicQuery: String): Result<RetrievalPlan> {
        val trimmed = topicQuery.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("주제가 비어 있습니다"))
        }

        return try {
            AgentTraceLogger.event("topic_planner", "start", mapOf("topicQuery" to trimmed))
            val prompt = GeminiTopicPlannerPrompt.build(trimmed)
            AgentTraceLogger.prompt("topic_planner", prompt)
            val rawResponse = geminiManager.run(prompt)
            AgentTraceLogger.rawResponse("topic_planner", rawResponse)
            val parseResult = GeminiAgentJsonParser.parseRetrievalPlan(rawResponse)

            parseResult.fold(
                onSuccess = { plan ->
                    AgentTraceLogger.parsed(
                        stage = "topic_planner",
                        message = "retrieval plan",
                        details = mapOf(
                            "keywords" to plan.keywords,
                            "typeFilters" to plan.typeFilters,
                            "dateRangeDays" to plan.dateRangeDays,
                            "maxResults" to plan.maxResults
                        )
                    )
                    if (validatePlan(plan)) {
                        Result.success(plan)
                    } else {
                        AgentTraceLogger.fallback("topic_planner", "invalid_parsed_plan")
                        fallbackTopicPlanner.plan(trimmed)
                    }
                },
                onFailure = { error ->
                    AgentTraceLogger.fallback("topic_planner", "parse_failed", error)
                    fallbackTopicPlanner.plan(trimmed)
                }
            )
        } catch (e: Exception) {
            // Gemini 호출 자체가 예외를 던진 경우 (네트워크 오류 등)
            AgentTraceLogger.fallback("topic_planner", "gemini_exception", e)
            fallbackTopicPlanner.plan(trimmed)
        }
    }

    /**
     * 파싱된 RetrievalPlan이 검색에 유효한지 검증한다.
     *
     * @return true if valid, false if fallback needed
     */
    private fun validatePlan(plan: RetrievalPlan): Boolean {
        // keywords가 비어 있으면 검색 의미 없음 → fallback
        if (plan.keywords.isEmpty()) return false

        // keywords 최대 8개 제한
        if (plan.keywords.size > 8) return false

        // maxResults 범위 확인
        if (plan.maxResults !in 5..50) return false

        // dateRangeDays가 null이 아니면 1..365 범위인지 확인
        val days = plan.dateRangeDays
        if (days != null && days !in 1..365) return false

        return true
    }
}
