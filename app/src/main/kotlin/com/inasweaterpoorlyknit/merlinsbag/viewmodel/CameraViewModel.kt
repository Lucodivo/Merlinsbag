package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.inasweaterpoorlyknit.core.common.Event
import com.inasweaterpoorlyknit.core.common.timestampFileName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    application: Application,
): AndroidViewModel(application) {

  enum class NavigationState{
    Back,
    SystemAppSettings,
  }

  var takePictureUri: Uri? = null
  var pictureInProgress = false
  var showPermissionsAlert by mutableStateOf(false)
  var navigationEventState by mutableStateOf(Event<NavigationState>(null))

  fun onNeverAskAgain() { showPermissionsAlert = true }
  fun onDismissPermissionsAlert() {
    showPermissionsAlert = false
    navigationEventState = Event(NavigationState.Back)
  }
  fun onConfirmPermissionsAlert() {
    showPermissionsAlert = false
    navigationEventState = Event(NavigationState.SystemAppSettings)
  }

  fun onCameraPermissionsLaunch() {
    pictureInProgress = true
  }

  fun onTakePicture(): Uri? {
    pictureInProgress = true
    val pictureFilename = "${timestampFileName()}.jpg"
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val contentResolver = getApplication<Application>().contentResolver
      val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, pictureFilename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        // TODO: Set as IS_PENDING before picture has been taken?
      }
      takePictureUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    } else {
      val publicPicturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
      val publicPictureFile = File(publicPicturesDir, pictureFilename)
      takePictureUri = publicPictureFile.toUri()
    }
    return takePictureUri
  }

  fun onPictureTaken(taken: Boolean) {
    if(!taken){
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
        val contentResolver = getApplication<Application>().contentResolver
        takePictureUri?.let { contentResolver.delete(it, null, null) }
        takePictureUri = null
      }
      navigationEventState = Event(NavigationState.Back)
    } else {
      takePictureUri = null
      pictureInProgress = false
    }
  }
}