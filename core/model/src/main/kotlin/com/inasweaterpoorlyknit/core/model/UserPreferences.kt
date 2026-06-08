package com.inasweaterpoorlyknit.core.model

import com.inasweaterpoorlyknit.core.model.preference.ColorPalette
import com.inasweaterpoorlyknit.core.model.preference.DarkMode
import com.inasweaterpoorlyknit.core.model.preference.HighContrast
import com.inasweaterpoorlyknit.core.model.preference.ImageQuality
import com.inasweaterpoorlyknit.core.model.preference.Typography

data class UserPreferences(
    val hasCompletedOnboarding: Boolean = false,
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val colorPalette: ColorPalette = ColorPalette.ROAD_WARRIOR,
    val highContrast: HighContrast = HighContrast.OFF,
    val typography: Typography = Typography.DEFAULT,
    val imageQuality: ImageQuality = ImageQuality.STANDARD,
)