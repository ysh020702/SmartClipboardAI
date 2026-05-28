package com.samsung.smartclipboard.domain.ai

import com.samsung.smartclipboard.domain.model.InputType
import com.samsung.smartclipboard.domain.model.LlmStructuredOutput

interface GeminiClient {
    suspend fun refineText(type: InputType, text: String): LlmStructuredOutput
}