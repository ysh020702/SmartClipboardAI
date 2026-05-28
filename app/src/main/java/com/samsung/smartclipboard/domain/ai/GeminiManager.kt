package com.samsung.smartclipboard.domain.ai

interface GeminiManager {
    suspend fun run(prompt: String): String
}