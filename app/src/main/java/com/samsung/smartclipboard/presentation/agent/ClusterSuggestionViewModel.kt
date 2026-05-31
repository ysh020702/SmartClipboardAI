package com.samsung.smartclipboard.presentation.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samsung.smartclipboard.domain.agent.ClusterTopicAgent
import com.samsung.smartclipboard.domain.repository.DataRepository
import com.samsung.smartclipboard.domain.retrieval.DataClusterer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClusterSuggestionViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val dataClusterer: DataClusterer,
    private val clusterTopicAgent: ClusterTopicAgent
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClusterSuggestionUiState())
    val uiState: StateFlow<ClusterSuggestionUiState> = _uiState.asStateFlow()

    init {
        generateSuggestions(force = false)
    }

    fun onIntent(intent: ClusterSuggestionIntent) {
        when (intent) {
            ClusterSuggestionIntent.Generate -> generateSuggestions(force = false)
            ClusterSuggestionIntent.Refresh -> generateSuggestions(force = true)
            ClusterSuggestionIntent.DismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun generateSuggestions(force: Boolean) {
        val current = _uiState.value
        if (current.isLoading) return
        if (!force && current.clusters.isNotEmpty()) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val items = dataRepository.observeItems().first()

                if (items.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            clusters = emptyList(),
                            suggestedTopics = emptyList(),
                            totalItemCount = 0,
                            generatedAt = System.currentTimeMillis(),
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    return@launch
                }

                val clusters = dataClusterer.cluster(items)
                if (clusters.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            clusters = emptyList(),
                            suggestedTopics = emptyList(),
                            totalItemCount = items.size,
                            generatedAt = System.currentTimeMillis(),
                            isLoading = false,
                            errorMessage = "추천할 만한 데이터 묶음을 찾지 못했습니다."
                        )
                    }
                    return@launch
                }

                val result = clusterTopicAgent.suggestTopics(clusters, items)
                val clustersWithTopics = result.getOrElse { throw it }

                val suggestedTopics = clustersWithTopics
                    .flatMap { it.topicCandidates }
                    .sortedByDescending { it.confidence }
                    .distinctBy { it.suggestedTitle }
                    .take(30)

                _uiState.update {
                    it.copy(
                        clusters = clustersWithTopics,
                        suggestedTopics = suggestedTopics,
                        totalItemCount = items.size,
                        generatedAt = System.currentTimeMillis(),
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "추천 주제 생성 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }
}