package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.Event
import com.inasweaterpoorlyknit.core.data.repository.UserPreferencesRepository
import com.inasweaterpoorlyknit.core.model.UserPreferences
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository
): ViewModel() {

  enum class LoadState {
    Loading,
    Success
  }

  var uiState by mutableStateOf(LoadState.Loading)
  var intentImageUriArgs by mutableStateOf(Event<List<String>>(null))
  var navigateToAddArticle by mutableStateOf(Event<Unit>(null))

  val userPreferences = userPreferencesRepository.userPreferences
      .onEach { uiState = LoadState.Success }
      .stateIn(
        scope = viewModelScope,
        initialValue = UserPreferences(
          hasCompletedOnboarding = true,
        ),
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
      )

  fun processIntent(intent: Intent) {
    val intentImageUris =
        if(intent.type?.startsWith("image/") == true) {
          when(intent.action){
            Intent.ACTION_SEND -> {
              if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
              } else {
                @Suppress("Deprecation")
                intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
              }?.let {
                listOf(it)
              } ?: emptyList()
            }
            Intent.ACTION_SEND_MULTIPLE -> {
              if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
              } else {
                @Suppress("Deprecation")
                intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.filterIsInstance<Uri>()
              } ?: emptyList()
            }
            else -> emptyList()
          }
        } else { emptyList() }
    if(intentImageUris.isNotEmpty()) {
      intentImageUriArgs = Event(intentImageUris.map { it.toString() })
    }
    if(intent.action == Intent.ACTION_VIEW){
      if(intent.dataString?.equals("camera") == true){
        navigateToAddArticle = Event(Unit)
      }
    }
  }
}