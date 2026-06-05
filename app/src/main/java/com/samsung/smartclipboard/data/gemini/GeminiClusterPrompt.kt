package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.domain.model.DataItem

/**
 * Gemini에게 purpose/purposeKeyword 기반 클러스터링을 요청하는 프롬프트 빌더.
 *
 * 입력: DataItem 목록 (purpose, purposeKeyword 포함)
 * 출력: 각 아이템의 클러스터 ID + 클러스터 라벨 JSON
 */
object GeminiClusterPrompt {

    private const val MAX_ITEMS = 50

    fun build(items: List<DataItem>): String {
        val limited = items.take(MAX_ITEMS)

        val itemLines = limited.mapIndexed { index, item ->
            val purpose = item.purpose?.takeIf { it.isNotBlank() } ?: "없음"
            val keywords = item.purposeKeyword?.takeIf { it.isNotBlank() } ?: "없음"
            val contentPreview = item.effectiveContent.take(150).replace("\n", " ")
            """${index + 1}. [id=${item.id}] purpose: $purpose | keywords: $keywords | 내용: $contentPreview"""
        }.joinToString("\n")

        return """
당신은 데이터 클러스터링 전문가입니다. 아래 데이터 항목들을 의미적으로 관련된 것끼리 그룹으로 묶어주세요.

## 입력 데이터 (${limited.size}개)
$itemLines

## 지시사항
1. 각 데이터 항목의 purpose(검색 목적)와 keywords(핵심 키워드)를 중심으로 관련 항목끼리 묶으세요.
2. 클러스터 ID는 1부터 시작하는 정수입니다. 관련 없는 항목은 서로 다른 클러스터에 배정하세요.
3. 클러스터 개수는 최대 10개까지 가능합니다. 단일 항목만 있는 클러스터도 허용됩니다.
4. 각 클러스터에 대해 간결한 한국어 라벨(15자 이내)을 작성하세요.

## 출력 형식
반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.
```json
{
  "assignments": [클러스터ID, 클러스터ID, ...],
  "clusters": {
    "1": "클러스터 라벨",
    "2": "클러스터 라벨"
  }
}
```

assignments 배열은 입력 데이터와 동일한 순서로 각 항목의 클러스터 ID를 나열합니다.
예: 항목1→클러스터1, 항목2→클러스터2, 항목3→클러스터1 이면 [1, 2, 1]

clusters 객체는 클러스터 ID(문자열)를 키로, 라벨을 값으로 합니다.
""".trimIndent()
    }
}