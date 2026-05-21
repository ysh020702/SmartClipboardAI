package com.samsung.smartclipboard.data.ai

import com.samsung.smartclipboard.domain.ai.AiProposalGenerator
import com.samsung.smartclipboard.domain.model.AiProposal
import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.DataItemType
import javax.inject.Inject

class HeuristicAiProposalGenerator @Inject constructor() : AiProposalGenerator {

    override suspend fun generateProposals(items: List<DataItem>): List<AiProposal> {
        if (items.size < 2) return emptyList()

        val proposals = mutableListOf<AiProposal>()
        val now = System.currentTimeMillis()

        // Category 1: Link groups (URLs in proximity)
        val links = items.filter { it.type == DataItemType.LINK }
        if (links.size >= 3) {
            val related = links.filter { link ->
                val domain = extractDomain(link.content)
                links.any { other -> other !== link && extractDomain(other.content) == domain }
            }
            if (related.size >= 2) {
                proposals.add(
                    AiProposal(
                        title = "Research links",
                        description = "You saved ${related.size} related URLs. Consider grouping them into a research note.",
                        confidence = 0.7f,
                        category = "links",
                        itemIds = related.map { it.id },
                        createdAt = now
                    )
                )
            }
        }

        // Category 2: Screenshot groups
        val screenshots = items.filter { it.type == DataItemType.SCREENSHOT }
        if (screenshots.size >= 3) {
            proposals.add(
                AiProposal(
                    title = "Screenshot collection",
                    description = "You have ${screenshots.size} screenshots. Group them into a visual note or document.",
                    confidence = 0.6f,
                    category = "screenshots",
                    itemIds = screenshots.map { it.id },
                    createdAt = now
                )
            )
        } else if (screenshots.isNotEmpty()) {
            proposals.add(
                AiProposal(
                    title = "Screenshot saved",
                    description = "A screenshot was collected. Attach it to a note or recipe document.",
                    confidence = 0.4f,
                    category = "screenshots",
                    itemIds = screenshots.map { it.id },
                    createdAt = now
                )
            )
        }

        // Category 3: Text + link proximity (text followed by link)
        val texts = items.filter { it.type == DataItemType.TEXT }
        if (texts.isNotEmpty() && links.isNotEmpty()) {
            proposals.add(
                AiProposal(
                    title = "Text with references",
                    description = "You saved ${texts.size} text items and ${links.size} links. They may form a reference document.",
                    confidence = 0.5f,
                    category = "mixed",
                    itemIds = (texts + links).map { it.id },
                    createdAt = now
                )
            )
        }

        // Category 4: Image groups
        val images = items.filter { it.type == DataItemType.IMAGE }
        if (images.size >= 3) {
            proposals.add(
                AiProposal(
                    title = "Image collection",
                    description = "You have ${images.size} images. Create a visual gallery or collage note.",
                    confidence = 0.6f,
                    category = "images",
                    itemIds = images.map { it.id },
                    createdAt = now
                )
            )
        }

        return proposals
    }

    private fun extractDomain(url: String): String {
        return try {
            val noProtocol = url.removePrefix("https://").removePrefix("http://").removePrefix("www.")
            noProtocol.substringBefore("/").substringBefore("?")
        } catch (e: Exception) {
            url
        }
    }
}