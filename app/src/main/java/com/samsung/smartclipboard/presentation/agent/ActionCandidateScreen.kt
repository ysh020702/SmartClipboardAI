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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samsung.smartclipboard.domain.model.AgentActionDraft

@Composable
fun ActionCandidateScreen(
    actionDrafts: List<AgentActionDraft>,
    selectedActionIndex: Int?,
    isLoading: Boolean,
    errorMessage: String?,
    onSelectAction: (Int) -> Unit,
    onNext: () -> Unit,
    onReset: () -> Unit,
    refineFeedback: String = "",
    onRefineFeedbackChange: (String) -> Unit = {},
    onStartRefinement: () -> Unit = {},
    onCancelRefinement: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "추천 작업 선택",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "선택한 아이템으로 수행할 작업을 고르세요.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (actionDrafts.isEmpty()) {
            Text(
                text = "생성된 작업 후보가 없습니다.",
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

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(actionDrafts) { index, action ->
                ActionDraftCard(
                    action = action,
                    isSelected = index == selectedActionIndex,
                    onClick = { onSelectAction(index) }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Refinement section
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AI 보완 요청",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "작업 후보가 원하는 방향과 다르면 조건을 입력해 다시 보완할 수 있습니다.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = refineFeedback,
            onValueChange = onRefineFeedbackChange,
            placeholder = { Text("예: 공유 초안 중심으로 짧게 다시 만들어줘") },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            maxLines = 3,
            enabled = !isLoading
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onCancelRefinement,
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                enabled = !isLoading
            ) {
                Text("입력 지우기")
            }
            Button(
                onClick = onStartRefinement,
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                enabled = refineFeedback.isNotBlank() && !isLoading
            ) {
                Text("AI 보완")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                enabled = selectedActionIndex != null && !isLoading
            ) {
                Text("도구 연결")
            }
        }
    }
}

@Composable
private fun ActionDraftCard(
    action: AgentActionDraft,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )

            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = action.type.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "신뢰도 ${(action.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = action.body.take(220).replace("\n", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = action.reason,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "연관 아이템 ${action.sourceItemIds.size}개",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}