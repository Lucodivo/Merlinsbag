package com.inasweaterpoorlyknit.core.model

// NOTE: This class is and should continue to be *TIGHTLY* coupled with user_preferences.proto
//   - ordinal of enums matter!

enum class DarkMode {
  SYSTEM,
  LIGHT,
  DARK,
}

enum class ColorPalette {
  DEFAULT,
  SYSTEM_DYNAMIC,
}

data class UserPreferences(
  val hasCompletedOnboarding: Boolean = false,
  val darkMode: DarkMode = DarkMode.SYSTEM,
  val colorPalette: ColorPalette = ColorPalette.DEFAULT,
)