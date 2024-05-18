package com.inasweaterpoorlyknit.inknit.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(): ViewModel() {
  var addArticle = mutableStateOf(Event<String>(null))
  var uriImageString: String? = null

  fun newImageUri(uriImageString: String){
    this.uriImageString = uriImageString
    addArticle.value = Event(uriImageString)
  }
}