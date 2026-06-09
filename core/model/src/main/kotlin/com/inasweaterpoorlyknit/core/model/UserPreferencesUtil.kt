package com.inasweaterpoorlyknit.core.model

import com.inasweaterpoorlyknit.core.model.proto.preference.ColorPalette.ColorPalette_RoadWarrior
import com.inasweaterpoorlyknit.core.model.proto.preference.DarkMode.DarkMode_System
import com.inasweaterpoorlyknit.core.model.proto.preference.HighContrast.HighContrast_Off
import com.inasweaterpoorlyknit.core.model.proto.preference.ImageQuality.ImageQuality_Standard
import com.inasweaterpoorlyknit.core.model.proto.preference.Typography.Typography_Default
import com.inasweaterpoorlyknit.core.model.proto.preference.UserPreferences

val UserPreferencesDefault = UserPreferences.newBuilder()
        .setHasCompletedOnboarding(false)
        .setDarkMode(DarkMode_System)
        .setColorPalette(ColorPalette_RoadWarrior)
        .setHighContrast(HighContrast_Off)
        .setTypography(Typography_Default)
        .setImageQuality(ImageQuality_Standard)
        .build()