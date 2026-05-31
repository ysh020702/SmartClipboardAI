package com.samsung.smartclipboard.presentation.agent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.ToolSpec

@Composable
fun ExecutionConfirmSheet(
    action: AgentActionDraft,
    toolSpec: ToolSpec,
    resolvedPayload: Map<String, String>,
    isLoading: Boolean,
    errorMessage: String?,
    onExecute: () -> Unit,
    onCancel: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "실행 전 확인",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "선택한 작업을 다음 도구로 실행합니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "안전을 위해 모든 도구 실행은 확인 후 진행됩니다.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action 정보
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("작업 정보", style = MaterialTheme.typography.titleSmall)
                Text("타입: ${action.type.name}", style = MaterialTheme.typography.bodySmall)
                Text("제목: ${action.title}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "본문: ${action.body.take(160).replace("\n", " ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ToolSpec 정보
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("실행 도구", style = MaterialTheme.typography.titleSmall)
                Text("도구: ${toolSpec.toolName}", style = MaterialTheme.typography.bodySmall)
                Text("설명: ${toolSpec.description}", style = MaterialTheme.typography.bodySmall)
                Text("위험도: ${toolSpec.riskLevel.name}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "확인 필요: ${if (toolSpec.requiresConfirmation) "예" else "아니오"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Payload 표시
        if (resolvedPayload.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("실행 데이터", style = MaterialTheme.typography.titleSmall)
                    resolvedPayload.forEach { (key, value) ->
                        val preview = if (value.length > 200) value.take(200) + "..." else value
                        Text(
                            text = "$key: $preview",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 버튼
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("취소")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("처음으로")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onExecute,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("실행")
        }
    }
}