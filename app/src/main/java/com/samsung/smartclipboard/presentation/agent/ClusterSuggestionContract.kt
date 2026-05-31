package com.samsung.smartclipboard.presentation.agent

import com.samsung.smartclipboard.domain.model.DataCluster
import com.samsung.smartclipboard.domain.model.SuggestedTopic

data class ClusterSuggestionUiState(
    val isLoading: Boolean = false,
    val clusters: List<DataCluster> = emptyList(),
    val suggestedTopics: List<SuggestedTopic> = emptyList(),
    val totalItemCount: Int = 0,
    val generatedAt: Long? = null,
    val errorMessage: String? = null
)

sealed interface ClusterSuggestionIntent {
    data object Generate : ClusterSuggestionIntent
    data object Refresh : ClusterSuggestionIntent
    data object DismissError : ClusterSuggestionIntent
}