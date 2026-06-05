package com.samsung.smartclipboard.data.agent

import com.samsung.smartclipboard.domain.ai.AnalyzedPurpose
import com.samsung.smartclipboard.domain.ai.PurposeAnalyzer
import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.DataItemType

/**
 * Gemini 호출 실패 시 사용하는 로컬 PurposeAnalyzer 폴백 구현체.
 *
 * 간단한 휴리스틱으로 purpose와 purposeKeyword를 생성한다.
 */
class FallbackPurposeAnalyzer : PurposeAnalyzer {

    override suspend fun analyze(items: List<DataItem>): Result<List<AnalyzedPurpose>> {
        if (items.isEmpty()) return Result.success(emptyList())

        return Result.success(
            items.map { item ->
                val purpose = buildFallbackPurpose(item)
                val keywords = buildFallbackKeywords(item, purpose)
                AnalyzedPurpose(
                    itemId = item.id,
                    purpose = purpose,
                    purposeKeyword = keywords
                )
            }
        )
    }

    private fun buildFallbackPurpose(item: DataItem): String {
        val contentHint = item.effectiveContent.take(60).replace("\n", " ").trim()
        val typeLabel = when (item.type) {
            DataItemType.TEXT -> "텍스트"
            DataItemType.LINK -> "링크"
            DataItemType.IMAGE -> "이미지"
            DataItemType.SCREENSHOT -> "스크린샷"
            DataItemType.FILE -> "파일"
        }

        val titleHint = item.title?.take(30)?.trim()

        return when {
            titleHint != null && titleHint.isNotBlank() ->
                "${typeLabel} 자료를 참고하기 위해 '${titleHint}' 관련 정보를 수집함"
            contentHint.isNotBlank() ->
                "${typeLabel} 형태의 '${contentHint}...' 관련 정보를 확인함"
            else ->
                "${typeLabel} 데이터를 참고하기 위해 수집함"
        }.take(200)
    }

    private fun buildFallbackKeywords(item: DataItem, purpose: String): String {
        val tokens = mutableListOf<String>()

        item.title?.split(Regex("[\\s,/.\n]+"))?.filter { it.length >= 2 }?.take(3)?.let { tokens.addAll(it) }

        item.effectiveContent
            .split(Regex("[\\s,/.\n]+"))
            .filter { it.length >= 2 }
            .take(3)
            .let { tokens.addAll(it) }

        if (tokens.isEmpty()) {
            tokens.addAll(purpose.split(Regex("[\\s,·]+")).filter { it.length >= 2 }.take(5))
        }

        return tokens.distinct().take(7).joinToString(",")
    }
}