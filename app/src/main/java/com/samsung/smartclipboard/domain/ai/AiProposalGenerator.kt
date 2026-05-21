package com.samsung.smartclipboard.domain.ai

import com.samsung.smartclipboard.domain.model.AiProposal
import com.samsung.smartclipboard.domain.model.DataItem

interface AiProposalGenerator {
    suspend fun generateProposals(items: List<DataItem>): List<AiProposal>
}