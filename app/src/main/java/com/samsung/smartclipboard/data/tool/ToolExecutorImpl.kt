package com.samsung.smartclipboard.data.tool

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.ToolExecutionResult
import com.samsung.smartclipboard.domain.model.ToolSpec
import com.samsung.smartclipboard.domain.tool.ToolExecutor
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class ToolExecutorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ToolExecutor {

    override suspend fun execute(
        sessionId: String,
        action: AgentActionDraft,
        toolSpec: ToolSpec,
        payload: Map<String, String>
    ): ToolExecutionResult {
        return try {
            when (toolSpec.toolName) {
                "copy_to_clipboard" -> executeCopyToClipboard(sessionId, toolSpec, payload)
                "share_text" -> executeShareText(sessionId, toolSpec, payload)
                "open_url" -> executeOpenUrl(sessionId, toolSpec, payload)
                "compose_email" -> executeComposeEmail(sessionId, toolSpec, payload)
                "save_note" -> executeSaveNote(sessionId, toolSpec, payload)
                else -> ToolExecutionResult(
                    resultId = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    toolName = toolSpec.toolName,
                    success = false,
                    message = "지원하지 않는 도구입니다.",
                    errorDetail = "unknown_tool: ${toolSpec.toolName}"
                )
            }
        } catch (e: Exception) {
            ToolExecutionResult(
                resultId = UUID.randomUUID().toString(),
                sessionId = sessionId,
                toolName = toolSpec.toolName,
                success = false,
                message = "도구 실행 중 오류가 발생했습니다.",
                errorDetail = e.message ?: "unknown_error"
            )
        }
    }

    private fun executeCopyToClipboard(
        sessionId: String,
        toolSpec: ToolSpec,
        payload: Map<String, String>
    ): ToolExecutionResult {
        val text = payload["textToCopy"]
        if (text.isNullOrBlank()) {
            return ToolExecutionResult(
                resultId = UUID.randomUUID().toString(),
                sessionId = sessionId,
                toolName = toolSpec.toolName,
                success = false,
                message = "복사할 텍스트가 없습니다.",
                errorDetail = "empty_textToCopy"
            )
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("SmartClipboard AI", text)
        clipboard.setPrimaryClip(clip)
        return ToolExecutionResult(
            resultId = UUID.randomUUID().toString(),
            sessionId = sessionId,
            toolName = toolSpec.toolName,
            success = true,
            message = "클립보드에 복사되었습니다."
        )
    }

    private fun executeShareText(
        sessionId: String,
        toolSpec: ToolSpec,
        payload: Map<String, String>
    ): ToolExecutionResult {
        val shareText = payload["shareText"]
        if (shareText.isNullOrBlank()) {
            return ToolExecutionResult(
                resultId = UUID.randomUUID().toString(),
                sessionId = sessionId,
                toolName = toolSpec.toolName,
                success = false,
                message = "공유할 내용이 없습니다.",
                errorDetail = "empty_shareText"
            )
        }
        return try {
            val title = payload["shareTitle"].orEmpty()
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, title.ifBlank { "공유" })
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            ToolExecutionResult(
                resultId = UUID.randomUUID().toString(),
                sessionId = sessionId,
                toolName = toolSpec.toolName,
                success = true,
                message = "공유 시트가 열렸습니다."
            )
        } catch (e: Exception) {
            ToolExecutionResult(
                resultId = UUID.randomUUID().toString(),
                sessionId = sessionId,
                toolName = toolSpec.toolName,
                success = false,
                message = "공유 시트를 열지 못했습니다.",
                errorDetail = e.message ?: "share_failed"
            )
        }
    }

    private fun executeOpenUrl(
        sessionId: String,
        toolSpec: ToolSpec,
        payload: Map<String, String>
    ): ToolExecutionResult {
        val url = payload["url"]
        if (url.isNullOrBlank()) {
            return ToolExecutionResult(
                resultId = UUID.randomUUID().toString(),
                sessionId = sessionId,
                toolName = toolSpec.toolName,
                success = false,
                message = "열 URL이 없습니다.",
                errorDetail = "empty_url"
            )
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return ToolExecutionResult(
                resultId = UUID.randomUUID().toString(),
                sessionId = sessionId,
                toolName = toolSpec.toolName,
                success = false,
                message = "안전하지 않은 URL은 열 수 없습니다.",
                errorDetail = "invalid_url_scheme"
            )
        }
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionResult(
                resultId = UUID.randomUUID().toString(),
                sessionId = sessionId,
                toolName = toolSpec.toolName,
                success = true,
                message = "URL을 열었습니다."
            )
        } catch (e: Exception) {
            ToolExecutionResult(
                resultId = UUID.randomUUID().toString(),
                sessionId = sessionId,
                toolName = toolSpec.toolName,
                success = false,
                message = "URL을 열지 못했습니다.",
                errorDetail = e.message ?: "open_url_failed"
            )
        }
    }

    private fun executeComposeEmail(
        sessionId: String,
        toolSpec: ToolSpec,
        payload: Map<String, String>
    ): ToolExecutionResult {
        val subject = payload["subject"]
        val body = payload["body"]
        if (subject.isNullOrBlank() || body.isNullOrBlank()) {
            return ToolExecutionResult(
                resultId = UUID.randomUUID().toString(),
                sessionId = sessionId,
                toolName = toolSpec.toolName,
                success = false,
                message = "이메일 제목과 본문이 필요합니다.",
                errorDetail = "empty_email_fields"
            )
        }
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                val to = payload["to"]
                if (!to.isNullOrBlank()) {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionResult(
                resultId = UUID.randomUUID().toString(),
                sessionId = sessionId,
                toolName = toolSpec.toolName,
                success = true,
                message = "이메일 앱이 열렸습니다."
            )
        } catch (e: Exception) {
            ToolExecutionResult(
                resultId = UUID.randomUUID().toString(),
                sessionId = sessionId,
                toolName = toolSpec.toolName,
                success = false,
                message = "이메일 앱을 열지 못했습니다.",
                errorDetail = e.message ?: "email_failed"
            )
        }
    }

    private fun executeSaveNote(
        sessionId: String,
        toolSpec: ToolSpec,
        payload: Map<String, String>
    ): ToolExecutionResult {
        // save_note는 repository 연동 없이 지원하지 않음
        return ToolExecutionResult(
            resultId = UUID.randomUUID().toString(),
            sessionId = sessionId,
            toolName = toolSpec.toolName,
            success = false,
            message = "내부 노트 저장은 아직 데이터 저장 API와 연결되지 않았습니다.",
            errorDetail = "save_note_unsupported_without_existing_repository_api"
        )
    }
}