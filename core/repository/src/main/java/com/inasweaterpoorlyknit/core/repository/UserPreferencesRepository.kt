package com.inasweaterpoorlyknit.core.repository

import com.inasweaterpoorlyknit.core.datastore.dao.UserPreferencesDao
import kotlinx.coroutines.flow.Flow

class UserPreferencesRepository(
    private val preferencesDao: UserPreferencesDao
) {
  val hasCompletedOnboarding: Flow<Boolean> = preferencesDao.hasCompletedOnboarding

  suspend fun setHasCompletedOnboarding(hasCompletedOnboarding: Boolean) {
    preferencesDao.setHasCompletedOnboarding(hasCompletedOnboarding)
  }
}