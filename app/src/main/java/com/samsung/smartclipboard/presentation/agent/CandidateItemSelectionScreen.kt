package com.samsung.smartclipboard.presentation.agent

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samsung.smartclipboard.domain.model.CandidateItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CandidateItemSelectionScreen(
    candidateItems: List<CandidateItem>,
    selectedItemIds: Set<Long>,
    recommendationReason: String,
    suggestedQueries: List<String>,
    isLoading: Boolean,
    errorMessage: String?,
    onToggleItem: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onNext: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "추천 아이템 선택",
            style = MaterialTheme.typography.headlineSmall
        )

        if (recommendationReason.isNotBlank()) {
            Text(
                text = recommendationReason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (candidateItems.isEmpty()) {
            Text(
                text = "검색된 후보 아이템이 없습니다.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            Button(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("처음으로")
            }
            return
        }

        // Selection controls
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "선택됨: ${selectedItemIds.size}개",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterVertically).weight(1f)
            )
            OutlinedButton(
                onClick = onSelectAll,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text("전체 선택", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(onClick = onClearSelection) {
                Text("선택 해제", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Candidate item list
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(candidateItems, key = { it.item.id }) { candidate ->
                CandidateItemCard(
                    candidate = candidate,
                    isSelected = candidate.item.id in selectedItemIds,
                    onToggle = { onToggleItem(candidate.item.id) }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Suggested queries
        if (suggestedQueries.isNotEmpty()) {
            Text(
                text = "추가 검색 제안: ${suggestedQueries.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom buttons
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) {
                Text("처음으로")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                enabled = selectedItemIds.isNotEmpty() && !isLoading
            ) {
                Text("다음으로")
            }
        }
    }
}

@Composable
private fun CandidateItemCard(
    candidate: CandidateItem,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val item = candidate.item
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )

            Column(modifier = Modifier.weight(1f)) {
                // Title or content preview
                val displayTitle = item.title?.takeIf { it.isNotBlank() }
                    ?: item.effectiveContent.take(120).replace("\n", " ")
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )

                // Type and score
                Row {
                    Text(
                        text = item.type.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "관련도 ${(candidate.relevanceScore * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Relevance reason
                if (candidate.relevanceReason.isNotBlank()) {
                    Text(
                        text = candidate.relevanceReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                // Source
                item.source?.takeIf { it.isNotBlank() }?.let { source ->
                    Text(
                        text = "출처: $source",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Created at
                Text(
                    text = dateFormat.format(Date(item.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
