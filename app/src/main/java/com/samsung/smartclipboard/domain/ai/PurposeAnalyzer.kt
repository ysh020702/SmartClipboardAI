package com.samsung.smartclipboard.domain.ai

import com.samsung.smartclipboard.domain.model.DataItem

/**
 * DataItem의 purpose와 purposeKeyword를 분석하는 인터페이스.
 *
 * purpose: 이 정보를 찾은 사람이 왜 찾았을까를 나타내는 목적 (문장 형태)
 * purposeKeyword: purpose의 키워드 단위 추출 (콤마 구분), 유사도 분석 및 클러스터링에 사용
 */
interface PurposeAnalyzer {

    /**
     * @param items purpose가 없는 DataItem 목록
     * @return purpose와 purposeKeyword가 채워진 DataItem 목록
     */
    suspend fun analyze(items: List<DataItem>): Result<List<AnalyzedPurpose>>
}

/**
 * Purpose 분석 결과
 */
data class AnalyzedPurpose(
    val itemId: Long,
    val purpose: String,
    val purposeKeyword: String
)