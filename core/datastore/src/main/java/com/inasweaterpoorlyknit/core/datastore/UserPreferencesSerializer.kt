package com.inasweaterpoorlyknit.core.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.inasweaterpoorlyknit.merlinsbag.ColorPalette as ColorPaletteDataStore
import com.inasweaterpoorlyknit.merlinsbag.DarkMode as DarkModeDataStore
import com.inasweaterpoorlyknit.merlinsbag.HighContrast as HighContrastDataStore
import com.inasweaterpoorlyknit.merlinsbag.UserPreferences as UserPreferencesDataStore
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class UserPreferencesSerializer @Inject constructor() : Serializer<UserPreferencesDataStore> {
  override val defaultValue: UserPreferencesDataStore = UserPreferencesDataStore.newBuilder()
      .setHasCompletedOnboarding(false)
      .setDarkMode(DarkModeDataStore.DarkMode_System)
      .setColorPalette(ColorPaletteDataStore.ColorPalette_RoadWarrior)
      .setHighContrast(HighContrastDataStore.HighContrast_Off)
      .build()

  override suspend fun readFrom(input: InputStream): UserPreferencesDataStore {
    try {
      return UserPreferencesDataStore.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
      throw CorruptionException("Cannot read proto.", exception)
    }
  }

  override suspend fun writeTo(
      t: UserPreferencesDataStore,
      output: OutputStream
  ) = t.writeTo(output)
}