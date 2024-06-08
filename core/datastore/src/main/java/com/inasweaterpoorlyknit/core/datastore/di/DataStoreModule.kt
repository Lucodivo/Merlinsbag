package com.inasweaterpoorlyknit.core.datastore.di


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.inasweaterpoorlyknit.core.common.di.ApplicationScope
import com.inasweaterpoorlyknit.core.common.di.Dispatcher
import com.inasweaterpoorlyknit.core.common.di.InjectDispatcher
import com.inasweaterpoorlyknit.core.datastore.UserPreferencesSerializer
import com.inasweaterpoorlyknit.core.datastore.dao.PurgeDataStoreDao
import com.inasweaterpoorlyknit.core.datastore.dao.UserPreferencesDao
import com.inasweaterpoorlyknit.merlinsbag.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
  @Provides
  @Singleton
  internal fun providesUserPreferencesDataStore(
      @ApplicationContext context: Context,
      @Dispatcher(InjectDispatcher.IO) ioDispatcher: CoroutineDispatcher,
      @ApplicationScope scope: CoroutineScope,
      userPreferencesSerializer: UserPreferencesSerializer,
  ): DataStore<UserPreferences> =
      DataStoreFactory.create(
        serializer = userPreferencesSerializer,
        scope = CoroutineScope(scope.coroutineContext + ioDispatcher),
        migrations = emptyList()
      ) {
        context.dataStoreFile("user_preferences.pb")
      }

  @Provides
  @Singleton
  fun providesUserPreferencesDao(
      preferencesDataStore: DataStore<UserPreferences>
  ): UserPreferencesDao = UserPreferencesDao(preferencesDataStore)

  @Provides
  @Singleton
  fun providesPurgeDataStoreDao(
      preferencesDataStore: DataStore<UserPreferences>
  ): PurgeDataStoreDao = PurgeDataStoreDao(preferencesDataStore)
}