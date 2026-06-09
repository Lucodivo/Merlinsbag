package com.inasweaterpoorlyknit.core.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.inasweaterpoorlyknit.core.model.UserPreferencesDefault
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import com.inasweaterpoorlyknit.core.model.proto.preference.UserPreferences as UserPreferencesDataStore

class UserPreferencesSerializer @Inject constructor() : Serializer<UserPreferencesDataStore> {
  override val defaultValue: UserPreferencesDataStore = UserPreferencesDefault

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