package com.samsung.smartclipboard.presentation.agent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.samsung.smartclipboard.domain.model.DataCluster
import com.samsung.smartclipboard.domain.model.SuggestedTopic
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClusterSuggestionScreen(
    viewModel: ClusterSuggestionViewModel = hiltViewModel(),
    onTopicSelected: (String, List<Long>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    compactMode: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    ClusterSuggestionScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onTopicSelected = onTopicSelected,
        modifier = modifier,
        compactMode = compactMode
    )
}

@Composable
fun ClusterSuggestionScreen(
    uiState: ClusterSuggestionUiState,
    onIntent: (ClusterSuggestionIntent) -> Unit,
    onTopicSelected: (String, List<Long>) -> Unit,
    modifier: Modifier = Modifier,
    compactMode: Boolean = false
) {
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    if (compactMode) {
        CompactModeContent(uiState, onIntent, onTopicSelected, modifier, dateFormat)
    } else {
        FullModeContent(uiState, onIntent, onTopicSelected, modifier, dateFormat)
    }
}

@Composable
private fun CompactModeContent(
    uiState: ClusterSuggestionUiState,
    onIntent: (ClusterSuggestionIntent) -> Unit,
    onTopicSelected: (String, List<Long>) -> Unit,
    modifier: Modifier,
    dateFormat: SimpleDateFormat
) {
    Column(modifier = modifier.padding(8.dp)) {
        Text(
            text = "AI가 제안하는 주제",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = "주제를 누르면 바로 에이전트가 시작됩니다.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )

        if (uiState.totalItemCount > 0) {
            Row(modifier = Modifier.padding(top = 4.dp)) {
                Text("데이터 ${uiState.totalItemCount}개", style = MaterialTheme.typography.labelSmall)
            }
        }

        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }

        OutlinedButton(
            onClick = { onIntent(ClusterSuggestionIntent.Refresh) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text(if (uiState.isLoading) "분석 중..." else "새로고침") }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp))
        } else if (uiState.suggestedTopics.isEmpty()) {
            Text("아직 추천할 주제가 없습니다.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            uiState.suggestedTopics.take(5).forEach { topic ->
                val clusterItemIds = uiState.clusters.firstOrNull { it.clusterId == topic.relatedClusterId }?.itemIds ?: emptyList()
                SuggestedTopicCard(topic = topic, clusterLabel = uiState.clusters.firstOrNull { it.clusterId == topic.relatedClusterId }?.clusterLabel, onClick = { onTopicSelected(topic.suggestedTitle, clusterItemIds) })
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun FullModeContent(
    uiState: ClusterSuggestionUiState,
    onIntent: (ClusterSuggestionIntent) -> Unit,
    onTopicSelected: (String, List<Long>) -> Unit,
    modifier: Modifier,
    dateFormat: SimpleDateFormat
) {
    Scaffold { innerPadding ->
        Column(
            modifier = modifier.fillMaxSize().padding(innerPadding).padding(16.dp)
        ) {
            Text("추천 주제", style = MaterialTheme.typography.headlineSmall)
            Text(
                "수집된 데이터를 묶어 AI에게 시킬 만한 주제를 제안합니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (uiState.totalItemCount > 0) {
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    Text("데이터 ${uiState.totalItemCount}개 · 클러스터 ${uiState.clusters.size}개", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(12.dp))
                    uiState.generatedAt?.let { ts -> Text(formatDate(ts, dateFormat), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { onIntent(ClusterSuggestionIntent.Refresh) }, enabled = !uiState.isLoading, modifier = Modifier.fillMaxWidth()) {
                Text(if (uiState.isLoading) "분석 중..." else "새로고침")
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 24.dp))
            } else if (uiState.suggestedTopics.isEmpty()) {
                Text("아직 추천할 주제가 없습니다.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Text("추천 주제 (${uiState.suggestedTopics.size}개)", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(uiState.suggestedTopics) { topic ->
                        val clusterItemIds = uiState.clusters.firstOrNull { it.clusterId == topic.relatedClusterId }?.itemIds ?: emptyList()
                        SuggestedTopicCard(topic = topic, clusterLabel = uiState.clusters.firstOrNull { it.clusterId == topic.relatedClusterId }?.clusterLabel, onClick = { onTopicSelected(topic.suggestedTitle, clusterItemIds) })
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (uiState.clusters.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(12.dp)); Text("데이터 묶음 (${uiState.clusters.size}개)", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(4.dp)) }
                        items(uiState.clusters) { cluster -> ClusterCard(cluster = cluster); Spacer(modifier = Modifier.height(4.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestedTopicCard(topic: SuggestedTopic, clusterLabel: String?, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(topic.suggestedTitle, style = MaterialTheme.typography.titleSmall)
            Text(topic.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            Row(modifier = Modifier.padding(top = 4.dp)) {
                Text("신뢰도 ${(topic.confidence * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                clusterLabel?.let { Spacer(modifier = Modifier.width(8.dp)); Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
            }
            Text(topic.reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun ClusterCard(cluster: DataCluster) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(cluster.clusterLabel, style = MaterialTheme.typography.titleSmall)
            Text("아이템 ${cluster.itemIds.size}개 · 추천 주제 ${cluster.topicCandidates.size}개", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

private fun formatDate(millis: Long, dateFormat: SimpleDateFormat): String {
    return try { dateFormat.format(Date(millis)) } catch (_: Exception) { millis.toString() }
}