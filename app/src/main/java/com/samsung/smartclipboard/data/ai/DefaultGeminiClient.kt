package com.samsung.smartclipboard.data.ai

import com.samsung.smartclipboard.domain.ai.GeminiClient
import com.samsung.smartclipboard.domain.ai.GeminiManager
import com.samsung.smartclipboard.domain.model.InputType
import com.samsung.smartclipboard.domain.model.LlmStructuredOutput
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultGeminiClient @Inject constructor(
    private val model: GeminiManager
) : GeminiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun refineText(type: InputType, text: String): LlmStructuredOutput {
        val contextGuide = when (type) {
            InputType.OCR -> "OCR 결과이므로 깨진 문장 복원"
            InputType.URL -> "웹페이지 노이즈 제거"
            InputType.TEXT -> "일반 텍스트 자연스럽게 정리"
        }

        val prompt = """
            역할: 비정형 텍스트를 구조화하고 "묶을 기준"까지 생성하는 시스템
            
            [출력 규칙]
            - JSON만 출력
            - 설명 절대 금지
            
            [JSON 스키마]
            {
              "title": "",
              "topic": "",
              "purpose": "",
              "summary": "",
              "keywords": ["", ""],
              "cleanedContent": "",
              "groupKey": "",
              "groupReason": ""
            }

            [핵심 규칙 ⭐]
            - groupKey:
              이 정보가 "다른 어떤 정보와 함께 묶여야 하는지"를 나타내는 대표 기준
              너무 일반적이면 안됨 (예: 맛집 ❌)
              너무 구체적이면 안됨 (예: 제주도 OO식당 ❌)
              👉 사용자 행동 기준으로 적절한 추상화

              예:
              - 제주도 여행 계획
              - 머신러닝 모델 학습
              - 아이폰 구매 비교

            - groupReason:
              왜 이렇게 묶는지 한 줄 설명

            ${contextGuide}

            [TEXT]
            ${text.take(1000)}
        """.trimIndent()

        val response = model.run(prompt)

        val clean = response
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return json.decodeFromString(clean)
    }
}