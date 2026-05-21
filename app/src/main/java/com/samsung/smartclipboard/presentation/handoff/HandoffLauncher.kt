package com.samsung.smartclipboard.presentation.handoff

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract

object HandoffLauncher {

    fun launchShareText(
        context: Context,
        title: String,
        body: String
    ): HandoffResult {
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            val chooser = Intent.createChooser(intent, "Send draft").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (chooser.resolveActivity(context.packageManager) != null) {
                context.startActivity(chooser)
                HandoffResult(isSuccess = true, message = "Draft shared")
            } else {
                HandoffResult(isSuccess = false, message = "No app found to share draft")
            }
        } catch (e: Exception) {
            HandoffResult(isSuccess = false, message = "Failed to share draft: ${e.message}")
        }
    }

    fun launchCalendarInsert(
        context: Context,
        title: String,
        description: String
    ): HandoffResult {
        return try {
            val now = System.currentTimeMillis()
            val beginTime = now + 60 * 60 * 1000L // 1 hour from now
            val endTime = beginTime + 60 * 60 * 1000L  // 2 hours from now
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                HandoffResult(isSuccess = true, message = "Calendar draft opened")
            } else {
                HandoffResult(isSuccess = false, message = "No calendar app found")
            }
        } catch (e: Exception) {
            HandoffResult(isSuccess = false, message = "Failed to open calendar: ${e.message}")
        }
    }
}