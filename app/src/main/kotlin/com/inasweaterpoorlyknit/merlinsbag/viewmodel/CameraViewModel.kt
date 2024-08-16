package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import com.inasweaterpoorlyknit.core.common.Event
import com.inasweaterpoorlyknit.core.common.timestampFileName
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File

@HiltViewModel(assistedFactory = CameraViewModel.CameraViewModelFactory::class)
class CameraViewModel @AssistedInject constructor(
    @Assisted("articleId") val articleId: String?,
    application: Application,
): AndroidViewModel(application) {

  @AssistedFactory
  interface CameraViewModelFactory {
    fun create(
        @Assisted("articleId") articleId: String?,
    ): CameraViewModel
  }

  companion object {
    private val TAG = "CameraViewModel"
  }

  sealed interface NavigationState{
    data object Back: NavigationState
    data object SystemAppSettings: NavigationState
    data class TakePicture(val tmpPhotoUri: Uri): NavigationState
    data class AddArticle(val uriStrings: List<String>, val articleId: String?): NavigationState
  }

  data class UiState (
      var launchCameraPermissions: Event<Unit>,
      var errorState: Event<ErrorState>,
      var showPermissionsAlert: Boolean,
      var navigationEventState: Event<NavigationState>,
  )


  enum class ErrorState {
    PhotoLost,
    PermissionsDenied,
  }

  private var inProgressPhotoUri: Uri? = null

  var uiState by mutableStateOf(
    UiState(
      launchCameraPermissions = Event(Unit),
      errorState = Event(null),
      showPermissionsAlert = false,
      navigationEventState = Event(null),
    )
  )

  fun onNeverAskAgain() { uiState = uiState.copy(showPermissionsAlert = true) }
  fun onDismissPermissionsAlert() {
    uiState = uiState.copy(
      showPermissionsAlert = false,
      navigationEventState = Event(NavigationState.Back)
    )
  }
  fun onConfirmPermissionsAlert() {
    uiState = uiState.copy(
      showPermissionsAlert = false,
      navigationEventState = Event(NavigationState.SystemAppSettings)
    )
  }

  fun onCameraPermissionsDenied() {
    uiState = uiState.copy(
      errorState = Event(ErrorState.PermissionsDenied),
      navigationEventState = Event(NavigationState.Back)
    )
  }

  fun onCameraPermissionsGranted() {
    val pictureFilename = "${timestampFileName()}.jpg"
    val tmpUri = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val contentResolver = getApplication<Application>().contentResolver
      val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, pictureFilename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        // TODO: Set as IS_PENDING before picture has been taken?
      }
      contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    } else {
      val publicPicturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
      val publicPictureFile = File(publicPicturesDir, pictureFilename)
      publicPictureFile.toUri()
    }
    if(tmpUri == null){
      Log.e(TAG, "Failed to generate temporary camera picture URI")
      uiState = uiState.copy(navigationEventState = Event(NavigationState.Back))
    } else {
      inProgressPhotoUri = tmpUri
      uiState = uiState.copy(navigationEventState = Event(NavigationState.TakePicture(tmpUri)))
    }
  }

  fun onPictureTaken(success: Boolean) {
    if(success){
      if(inProgressPhotoUri != null){
        uiState = uiState.copy(
          navigationEventState = Event(NavigationState.AddArticle(listOf(inProgressPhotoUri.toString()), articleId)),
          launchCameraPermissions = Event(Unit),
        )
      } else {
        Log.e(TAG, "Temp camera picture URI was null after picture was taken")
        uiState = uiState.copy(
          errorState = Event(ErrorState.PhotoLost),
          launchCameraPermissions = Event(Unit),
        )
      }
      inProgressPhotoUri = null
    } else {
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
        val contentResolver = getApplication<Application>().contentResolver
        inProgressPhotoUri?.let { contentResolver.delete(it, null, null) }
        inProgressPhotoUri = null
        uiState = uiState.copy(
          launchCameraPermissions = Event(Unit),
          navigationEventState = Event(NavigationState.Back)
        )
      } else uiState = uiState.copy(navigationEventState = Event(NavigationState.Back))
    }
  }
}