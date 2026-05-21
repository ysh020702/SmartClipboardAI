package com.samsung.smartclipboard.presentation.main

import com.samsung.smartclipboard.domain.model.AiProposal
import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.Topic
import com.samsung.smartclipboard.domain.model.TopicAction
import com.samsung.smartclipboard.domain.model.TopicAnalysis

enum class MainFilter {
    ALL,
    TEXT,
    LINK,
    IMAGE,
    FILE,
    SCREENSHOT
}

enum class MainScreenMode {
    HOME,
    DATA,
    TASKS,
    TOPIC_DETAIL
}

enum class TopicDetailTab {
    MATERIALS,
    ANALYSIS,
    ACTIONS
}

data class MainUiState(
    val screenMode: MainScreenMode = MainScreenMode.HOME,
    val workPrompt: String = "",
    val items: List<DataItem> = emptyList(),
    val visibleItems: List<DataItem> = emptyList(),
    val selectedFilter: MainFilter = MainFilter.ALL,
    val isLoading: Boolean = true,
    val totalCount: Int = 0,
    val textCount: Int = 0,
    val linkCount: Int = 0,
    val imageCount: Int = 0,
    val fileCount: Int = 0,
    val screenshotCount: Int = 0,
    val pendingDeleteItemId: Long? = null,
    val showClearAllConfirmDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val errorMessage: String? = null,
    // Media import fields
    val hasMediaPermission: Boolean = false,
    val isMediaImporting: Boolean = false,
    val mediaImportMessage: String? = null,
    val lastMediaImportCount: Int = 0,
    val showMediaPermissionBanner: Boolean = true,
    // AI proposal fields
    val proposals: List<AiProposal> = emptyList(),
    val isGeneratingProposals: Boolean = false,
    val proposalMessage: String? = null,
    // Topic/task fields
    val topics: List<Topic> = emptyList(),
    val selectedTopicId: Long? = null,
    val selectedTopicItems: List<DataItem> = emptyList(),
    val selectedTopicAnalysis: List<TopicAnalysis> = emptyList(),
    val selectedTopicActions: List<TopicAction> = emptyList(),
    val selectedTopicTab: TopicDetailTab = TopicDetailTab.MATERIALS,
    val isRunningTopicAnalysis: Boolean = false,
    // Selection and handoff fields
    val selectedItemIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val showHandoffSheet: Boolean = false,
    val handoffTitle: String = "",
    val handoffBody: String = "",
    val handoffHasPossibleDateTime: Boolean = false,
    val handoffCalendarTitle: String? = null,
    val handoffCalendarDescription: String? = null,
    val handoffActionMessage: String? = null,
    val handoffMessage: String? = null
)

sealed interface MainIntent {
    data object OpenDashboard : MainIntent
    data object OpenDataBrowser : MainIntent
    data object OpenTasks : MainIntent
    data class OpenTopicDetail(val topicId: Long) : MainIntent
    data class SelectTopicDetailTab(val tab: TopicDetailTab) : MainIntent
    data object OpenManualSelection : MainIntent
    data class UpdateWorkPrompt(val prompt: String) : MainIntent
    data object FindWithAi : MainIntent
    data class SelectFilter(val filter: MainFilter) : MainIntent
    data class RequestDeleteItem(val itemId: Long) : MainIntent
    data object ConfirmDeleteItem : MainIntent
    data object DismissDeleteDialog : MainIntent
    data object RequestClearAll : MainIntent
    data object ConfirmClearAll : MainIntent
    data object DismissClearAllDialog : MainIntent
    data object SnackbarShown : MainIntent
    // Media import intents
    data class MediaPermissionChanged(val hasPermission: Boolean) : MainIntent
    data object ImportRecentScreenshots : MainIntent
    data object DismissMediaImportMessage : MainIntent
    // AI proposal intents
    data object GenerateProposals : MainIntent
    data object DismissProposals : MainIntent
    // Selection and handoff intents
    data object EnterSelectionMode : MainIntent
    data object ExitSelectionMode : MainIntent
    data class ToggleItemSelection(val itemId: Long) : MainIntent
    data object ClearSelection : MainIntent
    data object SelectAllVisible : MainIntent
    data object OpenHandoffSheet : MainIntent
    data object DismissHandoffSheet : MainIntent
    data class UpdateHandoffTitle(val title: String) : MainIntent
    data class UpdateHandoffBody(val body: String) : MainIntent
    data object AddSelectionToTopic : MainIntent
    data object AddSelectionToTopicAndAnalyze : MainIntent
    data object RecommendActionsForSelection : MainIntent
    data object RunSelectedTopicAnalysis : MainIntent
    data class UpdateTopicActionDraft(val actionId: Long, val title: String, val body: String) : MainIntent
    data object DismissHandoffMessage : MainIntent
    data class HandoffActionCompleted(val message: String, val isSuccess: Boolean = true) : MainIntent
    data class UseProposalForHandoff(val itemIds: List<Long>) : MainIntent
}
