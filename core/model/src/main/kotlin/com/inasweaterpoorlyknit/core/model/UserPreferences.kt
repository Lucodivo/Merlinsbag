package com.inasweaterpoorlyknit.core.model

import com.inasweaterpoorlyknit.core.model.proto.preference.ColorPalette
import com.inasweaterpoorlyknit.core.model.proto.preference.DarkMode
import com.inasweaterpoorlyknit.core.model.proto.preference.HighContrast
import com.inasweaterpoorlyknit.core.model.proto.preference.ImageQuality
import com.inasweaterpoorlyknit.core.model.proto.preference.Typography

data class UserPreferences(
    val hasCompletedOnboarding: Boolean = false,
    val darkMode: DarkMode = DarkMode.DarkMode_System,
    val colorPalette: ColorPalette = ColorPalette.ColorPalette_RoadWarrior,
    val highContrast: HighContrast = HighContrast.HighContrast_Off,
    val typography: Typography = Typography.Typography_Default,
    val imageQuality: ImageQuality = ImageQuality.ImageQuality_Standard,
)