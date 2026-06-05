package com.samsung.smartclipboard.presentation.main



import androidx.lifecycle.ViewModel

import androidx.lifecycle.viewModelScope

import com.samsung.smartclipboard.data.source.media.MediaImportHandler

import com.samsung.smartclipboard.domain.model.DataItem

import com.samsung.smartclipboard.domain.model.DataItemType

import com.samsung.smartclipboard.domain.repository.DataRepository

import com.samsung.smartclipboard.presentation.handoff.HandoffDraftFormatter

import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.flow.flatMapLatest

import kotlinx.coroutines.flow.flowOf

import kotlinx.coroutines.flow.update

import kotlinx.coroutines.launch

import java.util.Calendar

import java.util.Locale

import javax.inject.Inject



@HiltViewModel

class MainViewModel @Inject constructor(

    private val dataRepository: DataRepository,

    private val mediaImportHandler: MediaImportHandler

) : ViewModel() {



    private val _filterState = MutableStateFlow(MainFilter.ALL)

    private val _selectedTopicId = MutableStateFlow<Long?>(null)

    private val _uiState = MutableStateFlow(MainUiState())



    val uiState: StateFlow<MainUiState> = _uiState



    init {

        observeItems()

        observeTopics()

        observeSelectedTopicDetails()

    }



    private fun observeItems() {

        viewModelScope.launch {

            dataRepository.observeItems().collect { items ->

                val filter = _filterState.value

                val filtered = applyFilter(items, filter)

                _uiState.update { current ->

                    val existingIds = items.map { it.id }.toSet()

                    val prunedSelection = current.selectedItemIds.filter { it in existingIds }.toSet()

                    current.copy(

                        items = items,

                        visibleItems = filtered,

                        isLoading = false,

                        totalCount = items.size,

                        textCount = items.count { it.type == DataItemType.TEXT },

                        linkCount = items.count { it.type == DataItemType.LINK },

                        imageCount = items.count { it.type == DataItemType.IMAGE },

                        fileCount = items.count { it.type == DataItemType.FILE },

                        screenshotCount = items.count { it.type == DataItemType.SCREENSHOT },

                        selectedFilter = filter,

                        selectedItemIds = prunedSelection,

                        isSelectionMode = current.isSelectionMode && items.isNotEmpty()

                    )

                }

            }

        }

    }



    private fun observeTopics() {

        viewModelScope.launch {

            dataRepository.observeTopics().collect { topics ->

                _uiState.update { it.copy(topics = topics) }

            }

        }

    }



    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

    private fun observeSelectedTopicDetails() {

        viewModelScope.launch {

            _selectedTopicId

                .flatMapLatest { topicId ->

                    topicId?.let { dataRepository.observeTopicItems(it) } ?: flowOf(emptyList())

                }

                .collect { items ->

                    _uiState.update { it.copy(selectedTopicItems = items) }

                }

        }

        viewModelScope.launch {

            _selectedTopicId

                .flatMapLatest { topicId ->

                    topicId?.let { dataRepository.observeTopicAnalysis(it) } ?: flowOf(emptyList())

                }

                .collect { analysis ->

                    _uiState.update { it.copy(selectedTopicAnalysis = analysis) }

                }

        }

        viewModelScope.launch {

            _selectedTopicId

                .flatMapLatest { topicId ->

                    topicId?.let { dataRepository.observeTopicActions(it) } ?: flowOf(emptyList())

                }

                .collect { actions ->

                    _uiState.update { it.copy(selectedTopicActions = actions) }

                }

        }

    }



    fun onIntent(intent: MainIntent) {

        when (intent) {

            is MainIntent.OpenDashboard -> {

                _selectedTopicId.value = null

                _uiState.update {

                    it.copy(

                        screenMode = MainScreenMode.HOME,

                        isSelectionMode = false,

                        selectedItemIds = emptySet(),

                        selectedTopicId = null,

                        selectedTopicItems = emptyList(),

                        selectedTopicAnalysis = emptyList(),

                        selectedTopicActions = emptyList()

                    )

                }

            }

            is MainIntent.OpenDataBrowser -> {

                _selectedTopicId.value = null

                _uiState.update {

                    it.copy(

                        screenMode = MainScreenMode.DATA_BROWSER,

                        isSelectionMode = false,

                        selectedTopicId = null,

                        selectedTopicItems = emptyList(),

                        selectedTopicAnalysis = emptyList(),

                        selectedTopicActions = emptyList()

                    )

                }

            }

            is MainIntent.OpenTasks -> {

                _selectedTopicId.value = null

                _uiState.update {

                    it.copy(

                        screenMode = MainScreenMode.HOME,

                        isSelectionMode = false,

                        selectedTopicId = null,

                        selectedTopicItems = emptyList(),

                        selectedTopicAnalysis = emptyList(),

                        selectedTopicActions = emptyList()

                    )

                }

            }

            is MainIntent.OpenTopicDetail -> {

                _selectedTopicId.value = intent.topicId

                _uiState.update {

                    it.copy(

                        screenMode = MainScreenMode.TOPIC_DETAIL,

                        selectedTopicId = intent.topicId,

                        selectedTopicTab = TopicDetailTab.MATERIALS,

                        isSelectionMode = false

                    )

                }

            }

            is MainIntent.SelectTopicDetailTab -> {

                _uiState.update { it.copy(selectedTopicTab = intent.tab) }

            }

            is MainIntent.OpenManualSelection -> {

                _uiState.update {

                    it.copy(

                        screenMode = MainScreenMode.MANUAL_SELECT,

                        isSelectionMode = true

                    )

                }

            }

            is MainIntent.UpdateWorkPrompt -> {

                _uiState.update { it.copy(workPrompt = intent.prompt) }

            }

            is MainIntent.FindWithAi -> {

                val current = _uiState.value

                val prompt = current.workPrompt.trim()

                if (prompt.isBlank()) {

                    _uiState.update { it.copy(snackbarMessage = "정리할 주제를 입력해 주세요") }

                    return

                }



                val candidates = findCandidateItemsForPrompt(prompt, current.items)

                if (candidates.isEmpty()) {

                    _uiState.update {

                        it.copy(snackbarMessage = "관련 데이터를 찾지 못했어요. 직접 고르기를 사용해 보세요.")

                    }

                    return

                }



                _uiState.update {

                    it.copy(

                        screenMode = MainScreenMode.MANUAL_SELECT,

                        isSelectionMode = true,

                        selectedItemIds = candidates.map { item -> item.id }.toSet(),

                        snackbarMessage = "관련 후보 ${candidates.size}개를 골랐어요"

                    )

                }

            }

            is MainIntent.SelectFilter -> {

                _filterState.value = intent.filter

                val items = _uiState.value.items

                _uiState.update {

                    it.copy(

                        selectedFilter = intent.filter,

                        visibleItems = applyFilter(items, intent.filter)

                    )

                }

            }

            is MainIntent.RequestDeleteItem -> {

                _uiState.update { it.copy(pendingDeleteItemId = intent.itemId) }

            }

            is MainIntent.ConfirmDeleteItem -> {

                val idToDelete = _uiState.value.pendingDeleteItemId ?: return

                viewModelScope.launch {

                    try {

                        dataRepository.deleteItem(idToDelete)

                        _uiState.update {

                            it.copy(

                                pendingDeleteItemId = null,

                                snackbarMessage = "Item deleted",

                                selectedItemIds = it.selectedItemIds - idToDelete,

                                isSelectionMode = it.isSelectionMode && (it.selectedItemIds - idToDelete).isNotEmpty()

                            )

                        }

                    } catch (e: Exception) {

                        _uiState.update {

                            it.copy(

                                pendingDeleteItemId = null,

                                snackbarMessage = "Failed to delete item"

                            )

                        }

                    }

                }

            }

            is MainIntent.DismissDeleteDialog -> {

                _uiState.update { it.copy(pendingDeleteItemId = null) }

            }

            is MainIntent.RequestClearAll -> {

                if (_uiState.value.items.isNotEmpty()) {

                    _uiState.update { it.copy(showClearAllConfirmDialog = true) }

                }

            }

            is MainIntent.ConfirmClearAll -> {

                viewModelScope.launch {

                    try {

                        dataRepository.clearAll()

                        _uiState.update {

                            it.copy(

                                showClearAllConfirmDialog = false,

                                snackbarMessage = "All items cleared",

                                selectedItemIds = emptySet(),

                                isSelectionMode = false,

                                showHandoffSheet = false

                            )

                        }

                    } catch (e: Exception) {

                        _uiState.update {

                            it.copy(

                                showClearAllConfirmDialog = false,

                                snackbarMessage = "Failed to clear items"

                            )

                        }

                    }

                }

            }

            is MainIntent.DismissClearAllDialog -> {

                _uiState.update { it.copy(showClearAllConfirmDialog = false) }

            }

            is MainIntent.SnackbarShown -> {

                _uiState.update { it.copy(snackbarMessage = null) }

            }

            is MainIntent.MediaPermissionChanged -> {

                _uiState.update {

                    it.copy(

                        hasMediaPermission = intent.hasPermission,

                        showMediaPermissionBanner = !intent.hasPermission

                    )

                }

            }

            is MainIntent.ImportRecentScreenshots -> {

                if (_uiState.value.isMediaImporting) return

                viewModelScope.launch {

                    _uiState.update { it.copy(isMediaImporting = true, mediaImportMessage = null) }

                    try {

                        val result = mediaImportHandler.importRecentScreenshots()

                        _uiState.update {

                            it.copy(

                                isMediaImporting = false,

                                mediaImportMessage = result.message,

                                lastMediaImportCount = result.importedCount,

                                showMediaPermissionBanner = !result.isSuccess &&

                                        result.message == "Media permission required"

                            )

                        }

                    } catch (e: Exception) {

                        _uiState.update {

                            it.copy(

                                isMediaImporting = false,

                                mediaImportMessage = "Failed to import screenshots"

                            )

                        }

                    }

                }

            }

            is MainIntent.DismissMediaImportMessage -> {

                _uiState.update { it.copy(mediaImportMessage = null) }

            }

            // Selection and handoff

            is MainIntent.EnterSelectionMode -> {

                _uiState.update {

                    it.copy(

                        screenMode = MainScreenMode.MANUAL_SELECT,

                        isSelectionMode = true

                    )

                }

            }

            is MainIntent.ExitSelectionMode -> {

                _selectedTopicId.value = null

                _uiState.update {

                    it.copy(

                        isSelectionMode = false,

                        selectedItemIds = emptySet(),

                        selectedTopicId = null,

                        selectedTopicItems = emptyList(),

                        selectedTopicAnalysis = emptyList(),

                        selectedTopicActions = emptyList()

                    )

                }

            }

            is MainIntent.ToggleItemSelection -> {

                _uiState.update { current ->

                    val newSelection = if (intent.itemId in current.selectedItemIds) {

                        current.selectedItemIds - intent.itemId

                    } else {

                        current.selectedItemIds + intent.itemId

                    }

                    current.copy(selectedItemIds = newSelection)

                }

            }

            is MainIntent.ClearSelection -> {

                _uiState.update { it.copy(selectedItemIds = emptySet()) }

            }

            is MainIntent.SelectAllVisible -> {

                _uiState.update { current ->

                    val allVisibleIds = current.visibleItems.map { it.id }.toSet()

                    current.copy(selectedItemIds = current.selectedItemIds + allVisibleIds)

                }

            }

            is MainIntent.OpenHandoffSheet -> {

                val selectedIds = _uiState.value.selectedItemIds

                if (selectedIds.isEmpty()) {

                    _uiState.update {

                        it.copy(handoffMessage = "Select at least one item")

                    }

                    return

                }

                val selectedItems = _uiState.value.items.filter { it.id in selectedIds }

                val draft = HandoffDraftFormatter.formatDraft(selectedItems)

                val existingTopicTitle = _uiState.value.topics

                    .firstOrNull { it.id == _uiState.value.selectedTopicId }

                    ?.title

                val topic = existingTopicTitle ?: _uiState.value.workPrompt.trim().ifBlank { draft.title }

                _uiState.update {

                    it.copy(

                        showHandoffSheet = true,

                        handoffTitle = topic,

                        handoffBody = draft.body,

                        handoffHasPossibleDateTime = draft.hasPossibleDateTime,

                        handoffCalendarTitle = draft.calendarTitle,

                        handoffCalendarDescription = draft.calendarDescription,

                        handoffActionMessage = null

                    )

                }

            }

            is MainIntent.DismissHandoffSheet -> {

                _uiState.update { it.copy(showHandoffSheet = false, handoffActionMessage = null) }

            }

            is MainIntent.UpdateHandoffTitle -> {

                _uiState.update { it.copy(handoffTitle = intent.title) }

            }

            is MainIntent.UpdateHandoffBody -> {

                val hasDt = HandoffDraftFormatter.hasPossibleDateTime(intent.body)

                _uiState.update {

                    it.copy(handoffBody = intent.body, handoffHasPossibleDateTime = hasDt)

                }

            }

            is MainIntent.AddSelectionToTopic -> {

                val current = _uiState.value

                val topic = current.handoffTitle.trim()

                if (topic.isBlank()) {

                    _uiState.update { it.copy(handoffActionMessage = "주제 또는 작업명을 입력해 주세요.") }

                    return

                }



                viewModelScope.launch {

                    try {

                        dataRepository.addItemsToTopic(

                            title = topic,

                            itemIds = current.selectedItemIds.toList(),

                            addedBy = "USER"

                        )

                        _uiState.update {

                            it.copy(

                                screenMode = MainScreenMode.TOPIC_DETAIL,

                                showHandoffSheet = false,

                                handoffActionMessage = null,

                                isSelectionMode = false,

                                selectedItemIds = emptySet(),

                                snackbarMessage = "\"$topic\"에 ${current.selectedItemIds.size}개 데이터를 추가했어요"

                            )

                        }

                    } catch (e: Exception) {

                        _uiState.update {

                            it.copy(handoffActionMessage = "주제에 데이터를 추가하지 못했어요.")

                        }

                    }

                }

            }

            is MainIntent.AddSelectionToTopicAndAnalyze -> {

                val current = _uiState.value

                val topic = current.handoffTitle.trim()

                if (topic.isBlank()) {

                    _uiState.update { it.copy(handoffActionMessage = "주제 또는 작업명을 입력해 주세요.") }

                    return

                }



                viewModelScope.launch {

                    _uiState.update { it.copy(isRunningTopicAnalysis = true, handoffActionMessage = null) }

                    try {

                        val topicId = dataRepository.addItemsToTopic(

                            title = topic,

                            itemIds = current.selectedItemIds.toList(),

                            addedBy = "USER"

                        )

                        // TODO(agent-pipeline): Replace the heuristic implementation in repository

                        // with a real Agent pipeline job queue and progress state.

                        val success = dataRepository.runTopicAnalysis(topicId)

                        _selectedTopicId.value = topicId

                        _uiState.update {

                            it.copy(

                                screenMode = MainScreenMode.TOPIC_DETAIL,

                                selectedTopicId = topicId,

                                selectedTopicTab = TopicDetailTab.ANALYSIS,

                                showHandoffSheet = false,

                                handoffActionMessage = null,

                                isRunningTopicAnalysis = false,

                                isSelectionMode = false,

                                selectedItemIds = emptySet(),

                                snackbarMessage = if (success) "\"$topic\" 분석을 완료했어요"

                                    else "\"$topic\" 분석에 실패했어요. 네트워크 연결을 확인해 주세요."

                            )

                        }

                    } catch (e: Exception) {

                        _uiState.update {

                            it.copy(

                                isRunningTopicAnalysis = false,

                                handoffActionMessage = "분석을 시작하지 못했어요."

                            )

                        }

                    }

                }

            }

            is MainIntent.RecommendActionsForSelection -> {

                val current = _uiState.value

                val selectedItems = current.items.filter { it.id in current.selectedItemIds }



                // TODO(agent-pipeline): Replace this heuristic with ActionSuggestionGenerator.

                // Expected output: calendar/reminder/summary/share/task-link actions with confidence and required item ids.

                _uiState.update {

                    it.copy(

                        handoffActionMessage = buildActionRecommendation(

                            topic = current.handoffTitle,

                            selectedItems = selectedItems

                        )

                    )

                }

            }

            is MainIntent.RunSelectedTopicAnalysis -> {

                val topicId = _uiState.value.selectedTopicId ?: return

                viewModelScope.launch {

                    _uiState.update { it.copy(isRunningTopicAnalysis = true) }

                    try {

                        val success = dataRepository.runTopicAnalysis(topicId)

                        _uiState.update {

                            it.copy(

                                selectedTopicTab = TopicDetailTab.ANALYSIS,

                                isRunningTopicAnalysis = false,

                                snackbarMessage = if (success) "분석 결과를 업데이트했어요"

                                    else "분석에 실패했어요. 네트워크 연결을 확인해 주세요."

                            )

                        }

                    } catch (e: Exception) {

                        _uiState.update {

                            it.copy(

                                isRunningTopicAnalysis = false,

                                snackbarMessage = "분석을 실행하지 못했어요"

                            )

                        }

                    }

                }

            }

            is MainIntent.UpdateTopicActionDraft -> {

                viewModelScope.launch {

                    try {

                        dataRepository.updateTopicActionDraft(

                            actionId = intent.actionId,

                            title = intent.title,

                            body = intent.body

                        )

                    } catch (e: Exception) {

                        _uiState.update { it.copy(snackbarMessage = "작업 초안을 수정하지 못했어요") }

                    }

                }

            }

            is MainIntent.DismissHandoffMessage -> {

                _uiState.update { it.copy(handoffMessage = null) }

            }

            is MainIntent.HandoffActionCompleted -> {

                if (intent.isSuccess) {

                    _uiState.update {

                        it.copy(

                            showHandoffSheet = false,

                            snackbarMessage = intent.message

                        )

                    }

                } else {

                    _uiState.update {

                        it.copy(

                            handoffMessage = intent.message

                        )

                    }

                }

            }

            is MainIntent.OpenAgent -> {

                _selectedTopicId.value = null

                _uiState.update {

                    it.copy(

                        screenMode = MainScreenMode.AGENT,

                        isSelectionMode = false,

                        selectedItemIds = emptySet(),

                        selectedTopicId = null,

                        selectedTopicItems = emptyList(),

                        selectedTopicAnalysis = emptyList(),

                        selectedTopicActions = emptyList()

                    )

                }

            }

            // New unified navigation intents
            is MainIntent.OpenAiSearch -> {
                _uiState.update {
                    it.copy(
                        screenMode = MainScreenMode.AI_SEARCH,
                        isSelectionMode = false
                    )
                }
            }

            is MainIntent.OpenManualSelect -> {
                _uiState.update {
                    it.copy(
                        screenMode = MainScreenMode.MANUAL_SELECT,
                        isSelectionMode = true,
                        selectedItemIds = emptySet()
                    )
                }
            }

            is MainIntent.OpenDataBrowserFromHome -> {
                _uiState.update {
                    it.copy(
                        screenMode = MainScreenMode.DATA_BROWSER,
                        isSelectionMode = false,
                        selectedItemIds = emptySet()
                    )
                }
            }

            is MainIntent.BackToHome -> {
                _selectedTopicId.value = null
                _uiState.update {
                    it.copy(
                        screenMode = MainScreenMode.HOME,
                        isSelectionMode = false,
                        selectedItemIds = emptySet(),
                        selectedTopicId = null,
                        selectedTopicItems = emptyList(),
                        selectedTopicAnalysis = emptyList(),
                        selectedTopicActions = emptyList()
                    )
                }
            }

            is MainIntent.UpdateTopicQuery -> {
                _uiState.update { it.copy(topicQuery = intent.query) }
            }

            is MainIntent.StartAgentWithCluster -> {
                val topicQuery = intent.topicQuery
                val clusterItemIds = intent.clusterItemIds
                _uiState.update {
                    it.copy(
                        screenMode = MainScreenMode.AGENT,
                        isSelectionMode = false,
                        selectedItemIds = clusterItemIds.toSet(),
                        handoffTitle = topicQuery,
                        topicQuery = topicQuery
                    )
                }
            }

            is MainIntent.StartAgentWithManualSelection -> {
                val topicQuery = intent.topicQuery
                val selectedItemIds = intent.selectedItemIds
                _uiState.update {
                    it.copy(
                        screenMode = MainScreenMode.AGENT,
                        handoffTitle = topicQuery,
                        selectedItemIds = selectedItemIds.toSet(),
                        isSelectionMode = false
                    )
                }
            }

        }

    }



    private fun applyFilter(items: List<DataItem>, filter: MainFilter): List<DataItem> {

        return when (filter) {

            MainFilter.ALL -> items

            MainFilter.TEXT -> items.filter { it.type == DataItemType.TEXT }

            MainFilter.LINK -> items.filter { it.type == DataItemType.LINK }

            MainFilter.IMAGE -> items.filter { it.type == DataItemType.IMAGE }

            MainFilter.FILE -> items.filter { it.type == DataItemType.FILE }

            MainFilter.SCREENSHOT -> items.filter { it.type == DataItemType.SCREENSHOT }

        }

    }



    private fun findCandidateItemsForPrompt(prompt: String, items: List<DataItem>): List<DataItem> {

        val normalized = prompt.lowercase(Locale.getDefault())

        val wantsToday = normalized.contains("오늘")

        val wantsYesterday = normalized.contains("어제")

        val wantsThisWeek = normalized.contains("이번 주") || normalized.contains("이번주")

        val requestedTypes = buildSet {

            if (normalized.contains("링크") || normalized.contains("url")) add(DataItemType.LINK)

            if (normalized.contains("메모") || normalized.contains("텍스트")) add(DataItemType.TEXT)

            if (normalized.contains("사진") || normalized.contains("이미지")) add(DataItemType.IMAGE)

            if (

                normalized.contains("스크린샷") ||

                normalized.contains("캡처") ||

                normalized.contains("캡쳐")

            ) {

                add(DataItemType.SCREENSHOT)

            }

            if (normalized.contains("파일") || normalized.contains("pdf")) add(DataItemType.FILE)

        }



        val tokens = normalized

            .split(Regex("\\s+"))

            .map { it.trim() }

            .filter { it.length >= 2 }

            .filterNot {

                it in setOf(

                    "오늘",

                    "어제",

                    "이번",

                    "이번주",

                    "관련",

                    "자료",

                    "정리",

                    "내용",

                    "찾아줘"

                )

            }



        return items.mapNotNull { item ->

            val searchable = listOfNotNull(

                item.title,

                item.effectiveContent,

                item.source,

                item.mimeType,

                item.type.name

            ).joinToString(" ").lowercase(Locale.getDefault())



            var score = 0

            if (requestedTypes.isNotEmpty() && item.type in requestedTypes) score += 2

            if (wantsToday && isSameDay(item.createdAt, 0)) score += 2

            if (wantsYesterday && isSameDay(item.createdAt, -1)) score += 2

            if (wantsThisWeek && isWithinDays(item.createdAt, 7)) score += 2

            score += tokens.count { searchable.contains(it) }



            if (score > 0) item to score else null

        }

            .sortedWith(compareByDescending<Pair<DataItem, Int>> { it.second }.thenByDescending { it.first.createdAt })

            .map { it.first }

            .take(20)

    }



    private fun isSameDay(timeMillis: Long, dayOffset: Int): Boolean {

        val target = Calendar.getInstance().apply {

            add(Calendar.DAY_OF_YEAR, dayOffset)

        }

        val itemDay = Calendar.getInstance().apply {

            timeInMillis = timeMillis

        }

        return target.get(Calendar.YEAR) == itemDay.get(Calendar.YEAR) &&

                target.get(Calendar.DAY_OF_YEAR) == itemDay.get(Calendar.DAY_OF_YEAR)

    }

    private fun isWithinDays(timeMillis: Long, days: Int): Boolean {

        val cutoff = System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L

        return timeMillis >= cutoff

    }



    private fun buildActionRecommendation(topic: String, selectedItems: List<DataItem>): String {

        if (selectedItems.isEmpty()) {

            return "선택된 데이터가 없습니다."

        }



        val hasDateCandidate = selectedItems.any {

            HandoffDraftFormatter.hasPossibleDateTime(it.effectiveContent)

        }

        val linkCount = selectedItems.count { it.type == DataItemType.LINK }

        val screenshotCount = selectedItems.count { it.type == DataItemType.SCREENSHOT }

        val textCount = selectedItems.count { it.type == DataItemType.TEXT }

        val normalizedTopic = topic.trim().ifBlank { "선택한 주제" }



        return buildString {

            append("\"$normalizedTopic\" 기준 추천 액션\n")

            if (hasDateCandidate) {

                append("- 일정 후보가 있어 캘린더 초안을 만들 수 있어요.\n")

            }

            if (linkCount >= 2) {

                append("- 링크 ${linkCount}개를 리서치 노트로 묶을 수 있어요.\n")

            }

            if (screenshotCount > 0) {

                append("- 스크린샷 ${screenshotCount}개는 OCR/이미지 분석 후 작업 자료로 붙이는 흐름이 적합해요.\n")

            }

            if (textCount > 0) {

                append("- 메모 ${textCount}개는 요약과 할 일 추출 후보입니다.\n")

            }

            append("- 기본 추천: 이 주제에 자료를 먼저 추가한 뒤 요약 액션을 생성하세요.")

        }

    }

}
