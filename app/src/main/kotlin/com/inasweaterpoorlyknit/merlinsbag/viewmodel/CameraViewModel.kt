package com.inasweaterpoorlyknit.merlinsbag.viewmodel

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
import androidx.lifecycle.ViewModel
import com.inasweaterpoorlyknit.core.common.Event
import com.inasweaterpoorlyknit.core.common.timestampFileName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    @ApplicationContext val context: Context,
): ViewModel() {

  var takePictureUri: Uri? = null
  var pictureInProgress = false
  var showPermissionsAlert by mutableStateOf(false)
  var finished by mutableStateOf(Event<Unit>(null))
  var launchSystemAppSettings by mutableStateOf(Event<Unit>(null))

  fun onNeverAskAgain() { showPermissionsAlert = true }
  fun onDismissPermissionsAlert() {
    showPermissionsAlert = false
    finished = Event(Unit)
  }
  fun onConfirmPermissionsAlert() {
    showPermissionsAlert = false
    launchSystemAppSettings = Event(Unit)
    finished = Event(Unit)
  }

  fun onCameraPermissionsLaunch() {
    pictureInProgress = true
  }

  fun onTakePicture(): Uri? {
    pictureInProgress = true
    val pictureFilename = "${timestampFileName()}.jpg"
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val contentResolver = context.contentResolver
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
        val contentResolver = context.contentResolver
        takePictureUri?.let { contentResolver.delete(it, null, null) }
        takePictureUri = null
      }
      finished = Event(Unit)
    } else {
      takePictureUri = null
      pictureInProgress = false
    }
  }
}