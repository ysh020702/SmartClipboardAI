package com.samsung.smartclipboard.data.gemini

import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.DataItemType

internal object GeminiPurposePrompt {

    fun build(items: List<DataItem>): String {
        val itemsJson = buildItemsJson(items)

        return buildString {
            append("너는 Android 앱에서 사용자의 수집 데이터를 분석하는 비서다.\n")
            append("각 데이터 항목에 대해, 사용자가 이 정보를 왜 찾았을지 그 '목적'을 추론해라.\n\n")

            append("## 목적(purpose)의 정의\n")
            append("- 글쓴이나 정보가 만들어진 목적이 아니다.\n")
            append("- 이 정보를 찾은 사람(사용자)이 왜 이 정보를 찾았을까를 추론하는 것이다.\n")
            append("- 예: '회의 준비를 위해 관련 자료를 조사함', '여행 일정 계획을 위해 항공권 정보를 찾음',\n")
            append("  '프로젝트 참고용으로 기술 문서를 수집함', '쇼핑 비교를 위해 가격 정보를 확인함'\n\n")

            append("## 반드시 지킬 규칙\n")
            append("- 응답은 반드시 JSON object 하나만 출력한다.\n")
            append("- markdown 코드 펜스, 설명문, 주석을 절대 포함하지 마라.\n")
            append("- 새 id를 생성하지 마라. 입력된 id만 사용.\n")
            append("- 한국어로 생성해라.\n")
            append("- 개인정보, URL, 주소, 연락처 등 민감한 값을 불필요하게 그대로 재출력하지 마라.\n\n")

            append("## 출력 JSON schema\n")
            append("{\n")
            append("  \"items\": [\n")
            append("    {\n")
            append("      \"id\": 1,\n")
            append("      \"purpose\": \"회의 준비를 위해 관련 자료를 조사함\",\n")
            append("      \"purposeKeyword\": \"회의,준비,자료,조사\"\n")
            append("    }\n")
            append("  ]\n")
            append("}\n\n")

            append("## 필드 규칙\n")
            append("- id: 입력된 아이템 id 중 하나만 사용\n")
            append("- purpose: 한국어 문장 형태, 10~80자, '함/임/위함' 등으로 끝나는 목적 서술\n")
            append("- purposeKeyword: purpose에서 추출한 핵심 키워드 3~7개, 콤마로 구분\n")
            append("  - 목적을 대표하는 명사/동명사 위주\n")
            append("  - 예: '회의,준비,자료조사', '여행,일정,계획,항공권', '쇼핑,가격비교'\n\n")

            append("## 데이터 항목 목록\n")
            append(itemsJson)
        }
    }

    private fun buildItemsJson(items: List<DataItem>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        items.forEachIndexed { index, item ->
            val comma = if (index < items.size - 1) "," else ""
            sb.append("  {\n")
            sb.append("    \"id\": ${item.id},\n")
            sb.append("    \"type\": \"${item.type.name}\",\n")
            sb.append("    \"title\": ${item.title?.let { "\"${escapeJson(it)}\"" } ?: "null"},\n")
            sb.append("    \"source\": ${item.source?.let { "\"${escapeJson(it)}\"" } ?: "null"},\n")
            sb.append("    \"contentPreview\": \"${escapeJson(contentPreview(item))}\",\n")
            sb.append("    \"createdAt\": ${item.createdAt}\n")
            sb.append("  }$comma\n")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun contentPreview(item: DataItem): String {
        return when (item.type) {
            DataItemType.FILE ->
                listOfNotNull(item.title, item.source, item.mimeType).joinToString(" / ").take(500)
            else -> item.effectiveContent.take(500)
        }
    }

    private fun escapeJson(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
}