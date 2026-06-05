package com.samsung.smartclipboard.data.gemini

import android.util.Log
import com.samsung.smartclipboard.data.agent.FallbackPurposeAnalyzer
import com.samsung.smartclipboard.domain.ai.AnalyzedPurpose
import com.samsung.smartclipboard.domain.ai.GeminiManager
import com.samsung.smartclipboard.domain.ai.PurposeAnalyzer
import com.samsung.smartclipboard.domain.model.DataItem

/**
 * Gemini LLM을 사용하여 DataItem의 purpose와 purposeKeyword를 분석하는 구현체.
 *
 * Gemini 호출 실패 시 FallbackPurposeAnalyzer로 폴백한다.
 * 한 번에 최대 20개 아이템을 처리하며, 초과 시 배치로 나누어 처리한다.
 */
class GeminiPurposeAnalyzer(
    private val geminiManager: GeminiManager,
    private val fallbackAnalyzer: PurposeAnalyzer = FallbackPurposeAnalyzer()
) : PurposeAnalyzer {

    companion object {
        private const val TAG = "GeminiPurposeAnalyzer"
        private const val BATCH_SIZE = 20
    }

    override suspend fun analyze(items: List<DataItem>): Result<List<AnalyzedPurpose>> {
        if (items.isEmpty()) return Result.success(emptyList())

        val allResults = mutableListOf<AnalyzedPurpose>()
        val batches = items.chunked(BATCH_SIZE)

        for (batch in batches) {
            val result = analyzeBatch(batch)
            result.fold(
                onSuccess = { allResults.addAll(it) },
                onFailure = {
                    Log.w(TAG, "Gemini purpose 분석 실패, 폴백 사용: ${it.message}")
                    val fallbackResult = fallbackAnalyzer.analyze(batch)
                    fallbackResult.fold(
                        onSuccess = { allResults.addAll(it) },
                        onFailure = { e -> Log.e(TAG, "폴백 purpose 분석도 실패: ${e.message}") }
                    )
                }
            )
        }

        return if (allResults.isEmpty()) {
            Result.failure(IllegalStateException("모든 purpose 분석이 실패했습니다"))
        } else {
            Result.success(allResults)
        }
    }

    private suspend fun analyzeBatch(batch: List<DataItem>): Result<List<AnalyzedPurpose>> {
        return try {
            val validIds = batch.map { it.id }.toSet()
            val prompt = GeminiPurposePrompt.build(batch)
            val rawResponse = geminiManager.run(prompt)

            val parseResult = GeminiPurposeJsonParser.parsePurposes(rawResponse, validIds)
            parseResult.fold(
                onSuccess = { result ->
                    if (validatePurposes(result, validIds)) Result.success(result)
                    else {
                        Log.w(TAG, "Purpose 검증 실패, 폴백 사용")
                        fallbackAnalyzer.analyze(batch)
                    }
                },
                onFailure = { e ->
                    Log.w(TAG, "Purpose 파싱 실패: ${e.message}")
                    fallbackAnalyzer.analyze(batch)
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "Gemini 호출 예외: ${e.message}")
            fallbackAnalyzer.analyze(batch)
        }
    }

    private fun validatePurposes(result: List<AnalyzedPurpose>, validIds: Set<Long>): Boolean {
        return result.all { analyzed ->
            analyzed.itemId in validIds &&
            analyzed.purpose.isNotBlank() &&
            analyzed.purposeKeyword.isNotBlank()
        }
    }
}