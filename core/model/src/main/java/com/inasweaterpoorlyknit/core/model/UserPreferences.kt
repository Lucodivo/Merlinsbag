package com.inasweaterpoorlyknit.core.model

enum class DarkMode {
  SYSTEM,
  LIGHT,
  DARK,
}

data class UserPreferences(
  val hasCompletedOnboarding: Boolean = false,
  val darkMode: DarkMode = DarkMode.SYSTEM,
)