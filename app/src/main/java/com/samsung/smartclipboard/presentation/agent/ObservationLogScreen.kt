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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samsung.smartclipboard.domain.model.ToolExecutionResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ObservationLogScreen(
    currentResult: ToolExecutionResult?,
    toolResults: List<ToolExecutionResult>,
    isCompleted: Boolean,
    errorMessage: String?,
    onFinish: () -> Unit,
    onRunAnotherAction: () -> Unit,
    onReset: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MM/dd HH:mm:ss", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (isCompleted) "완료" else "실행 결과",
            style = MaterialTheme.typography.headlineSmall
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

        // 현재 실행 결과 카드
        if (currentResult != null) {
            ExecutionResultCard(result = currentResult, dateFormat = dateFormat)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 실행 로그 섹션
        if (toolResults.isNotEmpty()) {
            Text(
                text = "실행 로그 (${toolResults.size}개)",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            HorizontalDivider()
            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(toolResults.sortedByDescending { it.executedAt }) { result ->
                    ExecutionLogItem(result = result, dateFormat = dateFormat)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 버튼 영역
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onRunAnotherAction,
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) {
                Text("다른 작업 실행")
            }
            if (isCompleted) {
                Button(
                    onClick = onReset,
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) {
                    Text("새로 시작")
                }
            } else {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) {
                    Text("완료")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("처음으로")
        }
    }
}

@Composable
private fun ExecutionResultCard(
    result: ToolExecutionResult,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.success)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (result.success) "✓ 성공" else "✗ 실패",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.toolName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = result.message.take(220),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            result.errorDetail?.let { detail ->
                Text(
                    text = detail.take(200),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = formatDate(result.executedAt, dateFormat),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ExecutionLogItem(
    result: ToolExecutionResult,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (result.success) "✓" else "✗",
                style = MaterialTheme.typography.bodySmall
            )
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    text = result.toolName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = result.message.take(120),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
                Text(
                    text = formatDate(result.executedAt, dateFormat),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDate(millis: Long, dateFormat: SimpleDateFormat): String {
    return try {
        dateFormat.format(Date(millis))
    } catch (_: Exception) {
        millis.toString()
    }
}