package com.samsung.smartclipboard.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samsung.smartclipboard.domain.repository.KnowledgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KnowledgeViewModel @Inject constructor(
    private val knowledgeRepository: KnowledgeRepository
) : ViewModel() {

    private val _results = MutableStateFlow<List<String>>(emptyList())
    val results: StateFlow<List<String>> = _results

    fun organize() {
        viewModelScope.launch {
            _results.value = knowledgeRepository.organize()
        }
    }
}