package com.samsung.smartclipboard.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val SamsungBlue = Color(0xFF0381FE)
val SamsungBlueDark = Color(0xFF0072DE)
val SamsungBlueDarkMode = Color(0xFF3E91FF)
val SamsungBlueSoft = Color(0xFFEAF4FF)

val OneUiBackground = Color(0xFFFAFAFA)
val OneUiSurface = Color(0xFFFFFFFF)
val OneUiSurfaceRaised = Color(0xFFF5F6F8)
val OneUiOutline = Color(0xFFE3E5EA)
val OneUiText = Color(0xFF111214)
val OneUiTextSecondary = Color(0xFF6F737A)

val OneUiDarkBackground = Color(0xFF080808)
val OneUiDarkSurface = Color(0xFF17181A)
val OneUiDarkSurfaceRaised = Color(0xFF222327)
val OneUiDarkOutline = Color(0xFF34363B)
val OneUiDarkText = Color(0xFFFAFAFA)
val OneUiDarkTextSecondary = Color(0xFFB7BBC2)

val OneUiPositive = Color(0xFF18A058)
val OneUiWarning = Color(0xFFFFA726)
val OneUiDanger = Color(0xFFEA3323)

object AppColors {
    val Blue = Color(0xFF1D4ED8)
    val BlueDeep = Color(0xFF1E3A8A)
    val BlueSoft = Color(0xFFEFF6FF)
    val Cyan = Color(0xFF0891B2)
    val Green = Color(0xFF059669)
    val Slate900 = Color(0xFF0F172A)
    val Slate800 = Color(0xFF1E293B)
    val Slate500 = Color(0xFF64748B)
    val Slate400 = Color(0xFF94A3B8)
    val Slate200 = Color(0xFFE2E8F0)
    val Surface = Color(0xFFF8FAFC)
    val Border = Color(0xFFE8EDF8)
    val Red = Color(0xFFDC2626)
}

val BlueGradient = Brush.linearGradient(listOf(AppColors.BlueDeep, AppColors.Blue, Color(0xFF3B82F6)))
val DarkGradient = Brush.linearGradient(listOf(Color(0xFF0F1F3D), Color(0xFF1A3660), AppColors.BlueDeep))

