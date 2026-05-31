package com.samsung.smartclipboard.data.gemini

/**
 * GeminiTopicPlanner에 전달할 프롬프트를 생성하는 유틸.
 *
 * 사용자 주제 → Android 로컬 데이터 검색 계획(RetrievalPlan)으로 변환하는
 * JSON-only 응답을 지시한다.
 */
internal object GeminiTopicPlannerPrompt {

    fun build(topicQuery: String): String {
        return buildString {
            append("너는 Android 로컬 데이터 검색 계획을 수립하는 비서다.\n")
            append("사용자의 주제에 맞는 데이터를 Android 기기 내에서 찾기 위한 검색 계획을 JSON으로 출력해라.\n\n")

            append("## 반드시 지킬 규칙\n")
            append("- 응답은 반드시 JSON object 하나만 출력한다.\n")
            append("- markdown 코드 펜스(\\`\\`\\`), 설명문, 주석을 절대 포함하지 마라.\n")
            append("- JSON object 외에 다른 텍스트를 출력하지 마라.\n\n")

            append("## 출력 JSON schema\n")
            append("{\n")
            append("  \"keywords\": [\"string\"],\n")
            append("  \"typeFilters\": [\"TEXT\"|\"LINK\"|\"IMAGE\"|\"FILE\"|\"SCREENSHOT\"],\n")
            append("  \"dateRangeDays\": null 또는 숫자,\n")
            append("  \"maxResults\": 숫자\n")
            append("}\n\n")

            append("## 필드 규칙\n")
            append("- keywords: 한국어 조사(은/는/이/가/을/를 등)는 제거한 핵심 명사/동사 중심 키워드 1~8개.\n")
            append("  필요하면 영어 동의어를 함께 포함해라. 빈 문자열 금지. 중복 금지.\n")
            append("- typeFilters: 다음 중 검색 대상 타입. 없으면 빈 배열.\n")
            append("  TEXT(메모/글/텍스트), LINK(링크/URL/주소/웹사이트),\n")
            append("  IMAGE(사진/이미지), FILE(파일/문서/PDF/첨부), SCREENSHOT(스크린샷/캡처)\n")
            append("- dateRangeDays: 시간 조건이 있으면 일수(1~365). 없으면 null.\n")
            append("  오늘→1, 어제→2, 이번 주→7, 지난주→14, 이번 달→30, 지난달→60, 최근→30\n")
            append("- maxResults: 기본값 20. 5~50 사이.\n\n")

            append("## 예시\n")
            append("주제: \"오늘 수집한 링크와 메모 정리\"\n")
            append("응답: {\"keywords\":[\"링크\",\"메모\",\"정리\",\"link\",\"memo\"],\"typeFilters\":[\"LINK\",\"TEXT\"],\"dateRangeDays\":1,\"maxResults\":20}\n\n")

            append("주제: \"이번 주 업무 관련 자료\"\n")
            append("응답: {\"keywords\":[\"업무\",\"회의\",\"일정\",\"보고서\",\"work\",\"meeting\"],\"typeFilters\":[],\"dateRangeDays\":7,\"maxResults\":30}\n\n")

            append("## 사용자 주제\n")
            append(topicQuery)
        }
    }
}