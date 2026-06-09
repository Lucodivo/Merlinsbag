package com.inasweaterpoorlyknit.core.data.repository

import com.inasweaterpoorlyknit.core.datastore.dao.UserPreferencesDao
import com.inasweaterpoorlyknit.core.model.proto.preference.ColorPalette
import com.inasweaterpoorlyknit.core.model.proto.preference.DarkMode
import com.inasweaterpoorlyknit.core.model.proto.preference.HighContrast
import com.inasweaterpoorlyknit.core.model.proto.preference.ImageQuality
import com.inasweaterpoorlyknit.core.model.proto.preference.Typography
import com.inasweaterpoorlyknit.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow

class UserPreferencesRepository(
    private val preferencesDao: UserPreferencesDao
) {
  val userPreferences: Flow<UserPreferences> = preferencesDao.userPreferences

  suspend fun setHasCompletedOnboarding(hasCompletedOnboarding: Boolean) {
    preferencesDao.setHasCompletedOnboarding(hasCompletedOnboarding)
  }

  suspend fun setDarkMode(darkMode: DarkMode){
    preferencesDao.setDarkMode(darkMode)
  }

  suspend fun setColorPalette(colorPalette: ColorPalette) {
    preferencesDao.setColorPalette(colorPalette)
  }

  suspend fun setHighContrast(highContrast: HighContrast) {
    preferencesDao.setHighContrast(highContrast)
  }

  suspend fun setTypography(typography: Typography) {
    preferencesDao.setTypography(typography)
  }

  suspend fun setImageQuality(imageQuality: ImageQuality) {
    preferencesDao.setImageQuality(imageQuality)
  }
}