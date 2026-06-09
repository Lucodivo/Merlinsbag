package com.inasweaterpoorlyknit.core.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import com.inasweaterpoorlyknit.core.model.proto.preference.ColorPalette.ColorPalette_RoadWarrior
import com.inasweaterpoorlyknit.core.model.proto.preference.DarkMode.DarkMode_System
import com.inasweaterpoorlyknit.core.model.proto.preference.HighContrast.HighContrast_Off
import com.inasweaterpoorlyknit.core.model.proto.preference.ImageQuality.ImageQuality_Standard
import com.inasweaterpoorlyknit.core.model.proto.preference.Typography.Typography_Default
import com.inasweaterpoorlyknit.core.model.proto.preference.UserPreferences as UserPreferencesDataStore

class UserPreferencesSerializer @Inject constructor() : Serializer<UserPreferencesDataStore> {
  override val defaultValue: UserPreferencesDataStore = defaultUserPreferences

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

  companion object {
    val defaultUserPreferences: UserPreferencesDataStore = UserPreferencesDataStore.newBuilder()
        .setHasCompletedOnboarding(false)
        .setDarkMode(DarkMode_System)
        .setColorPalette(ColorPalette_RoadWarrior)
        .setHighContrast(HighContrast_Off)
        .setTypography(Typography_Default)
        .setImageQuality(ImageQuality_Standard)
        .build()

  }
}