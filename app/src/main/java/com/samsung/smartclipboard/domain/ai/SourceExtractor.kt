package com.samsung.smartclipboard.domain.ai

interface SourceExtractor {
    suspend fun extractFromOcr(uriString: String): String
    suspend fun extractFromUrl(url: String): String
}