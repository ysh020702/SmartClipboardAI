package com.samsung.smartclipboard.data.tool

import com.samsung.smartclipboard.domain.model.RequiredInput
import com.samsung.smartclipboard.domain.model.ToolRiskLevel
import com.samsung.smartclipboard.domain.model.ToolSpec
import com.samsung.smartclipboard.domain.tool.ToolRegistry

class ToolRegistryImpl : ToolRegistry {

    private val tools: List<ToolSpec> = listOf(
        ToolSpec(
            toolName = "copy_to_clipboard",
            description = "텍스트를 클립보드에 복사",
            riskLevel = ToolRiskLevel.LOW,
            requiresConfirmation = false,
            androidAction = "android.content.ClipboardManager",
            requiredInputs = listOf(
                RequiredInput(key = "textToCopy", label = "복사할 텍스트", required = true)
            )
        ),
        ToolSpec(
            toolName = "share_text",
            description = "Android 공유 시트로 텍스트 공유",
            riskLevel = ToolRiskLevel.LOW,
            requiresConfirmation = true,
            androidAction = "android.intent.action.SEND",
            requiredInputs = listOf(
                RequiredInput(key = "shareTitle", label = "공유 제목", required = false),
                RequiredInput(key = "shareText", label = "공유 내용", required = true)
            )
        ),
        ToolSpec(
            toolName = "open_url",
            description = "브라우저 또는 연결 앱에서 URL 열기",
            riskLevel = ToolRiskLevel.LOW,
            requiresConfirmation = true,
            androidAction = "android.intent.action.VIEW",
            requiredInputs = listOf(
                RequiredInput(key = "url", label = "열 URL", required = true)
            )
        ),
        ToolSpec(
            toolName = "compose_email",
            description = "이메일 앱에서 초안 작성",
            riskLevel = ToolRiskLevel.MEDIUM,
            requiresConfirmation = true,
            androidAction = "android.intent.action.SENDTO",
            requiredInputs = listOf(
                RequiredInput(key = "to", label = "받는 사람", required = false),
                RequiredInput(key = "subject", label = "제목", required = true),
                RequiredInput(key = "body", label = "본문", required = true)
            )
        ),
        ToolSpec(
            toolName = "save_note",
            description = "앱 내부 노트로 저장",
            riskLevel = ToolRiskLevel.LOW,
            requiresConfirmation = false,
            androidAction = "internal.save_note",
            requiredInputs = listOf(
                RequiredInput(key = "noteTitle", label = "노트 제목", required = true),
                RequiredInput(key = "noteBody", label = "노트 내용", required = true)
            )
        )
    )

    override fun getAllTools(): List<ToolSpec> = tools

    override fun getTool(toolName: String): ToolSpec? = tools.firstOrNull { it.toolName == toolName }
}