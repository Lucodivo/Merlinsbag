package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.Event
import com.inasweaterpoorlyknit.core.common.profiling.Timer
import com.inasweaterpoorlyknit.core.common.timestampFileName
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.repository.UserPreferencesRepository
import com.inasweaterpoorlyknit.core.ml.image.SegmentedImage
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

@HiltViewModel(assistedFactory = AddArticleViewModel.AddArticleViewModelFactory::class)
class AddArticleViewModel @AssistedInject constructor(
    @Assisted("imageUriStrings") imageUriStrings: List<String>,
    @Assisted("articleId") private val articleId: String?,
    application: Application,
    private val articleRepository: ArticleRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val segmentedImage: SegmentedImage
): AndroidViewModel(application) {

  companion object {
    private val rotations = arrayOf(0.0f, 90.0f, 180.0f, 270.0f)
    private val TAG = "AddArticleViewModel"
  }

  @AssistedFactory
  interface AddArticleViewModelFactory {
    fun create(
        @Assisted("imageUriStrings") uriImageStrings: List<String>,
        @Assisted("articleId") articleId: String?,
    ): AddArticleViewModel
  }

  enum class DialogState {
    None,
    Discard,
    Attach,
    CameraPermissions,
  }

  sealed interface NavigationState{
    data object Back: NavigationState
    data object SystemAppSettings: NavigationState
    data class TakePicture(val tmpPhotoUri: Uri): NavigationState
  }

  sealed interface ErrorState {
    data object PhotoLost: ErrorState
    data object PermissionsDenied: ErrorState
    data class ProcessingError(@StringRes val msgId: Int): ErrorState
  }

  private sealed interface ProcessingState {
    data class Camera(
        val inProgressPhotoUri: Uri?,
    ): ProcessingState
    data class Album(
        val imageUriStrings: List<String>,
        val processingImageIndex: Int
    ): ProcessingState
  }

  private var processingState = if(imageUriStrings.isEmpty()){
    ProcessingState.Camera(inProgressPhotoUri = null)
  } else {
    ProcessingState.Album(imageUriStrings, -1)
  }
  private var imageRotationIndex = 0
  private var attachArticleThumbnailsCache: LazyArticleThumbnails? = null

  var launchCameraPermissions by mutableStateOf(Event(if(processingState is ProcessingState.Camera) Unit else null))
  var processing by mutableStateOf(true)
  var processedBitmap by mutableStateOf<Bitmap?>(null)
  var rotation by mutableFloatStateOf(rotations[imageRotationIndex])
  var navigationEventState by mutableStateOf(Event<NavigationState>(null))
  var errorState by mutableStateOf(Event<ErrorState>(null))
  var attachArticleIndex by mutableStateOf<Int?>(null)
  var dialogState by mutableStateOf(DialogState.None)
  val attachToArticleEnabled = articleId == null
  val attachArticleThumbnails: StateFlow<LazyUriStrings> = articleRepository.getAllArticlesWithThumbnails()
      .onEach { this.attachArticleThumbnailsCache = it }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = LazyUriStrings.Empty
      )

  init { if(processingState is ProcessingState.Album) nextAlbumUri() }

  override fun onCleared() {
    super.onCleared()
    segmentedImage.cleanup()
  }

  fun removeAttachedArticle() { attachArticleIndex = null }
  fun addAttachedArticle(articleIndex: Int) { attachArticleIndex = articleIndex }

  private fun dismissDialog() {
    if(dialogState != DialogState.None) dialogState = DialogState.None
  }

  fun onClickDiscard() { dialogState = DialogState.Discard }
  fun onDismissDiscardDialog() = dismissDialog()
  fun onDiscard() {
    dismissDialog()
    nextSubject()
  }

  fun onClickAttach() { dialogState = DialogState.Attach }
  fun onDismissAttachDialog() = dismissDialog()

  fun onWidenClicked() {
    processing = true
    segmentedImage.decreaseThreshold()
    refreshProcessedBitmap()
  }

  fun onFocusClicked() {
    processing = true
    segmentedImage.increaseThreshold()
    refreshProcessedBitmap()
  }

  fun onRotateCW() {
    imageRotationIndex = if(imageRotationIndex == rotations.lastIndex) 0 else imageRotationIndex + 1
    rotation = rotations[imageRotationIndex]
  }

  fun onRotateCCW() {
    imageRotationIndex = if(imageRotationIndex == 0) rotations.lastIndex else imageRotationIndex - 1
    rotation = rotations[imageRotationIndex]
  }

  fun onSave() {
    dismissDialog()
    val stopWatch = Timer()
    lateinit var bitmapToSave: Bitmap
    val rotationMatrix = Matrix()
    rotationMatrix.postRotate(rotation)
    bitmapToSave = Bitmap.createBitmap(
      segmentedImage.subjectBitmap,
      segmentedImage.subjectBoundingBox.minX,
      segmentedImage.subjectBoundingBox.minY,
      segmentedImage.subjectBoundingBox.width,
      segmentedImage.subjectBoundingBox.height,
      rotationMatrix,
      false // e.g. bilinear filtering of source
    )
    val attachmentArticleIndex = attachArticleIndex
    val attachmentArticleId = if(attachmentArticleIndex != null){
      attachArticleIndex= null
      attachArticleThumbnailsCache?.getArticleId(attachmentArticleIndex)
    } else articleId
    viewModelScope.launch(Dispatchers.Default) {
      nextSubject()
      val imageQuality = userPreferencesRepository.userPreferences.first().imageQuality
      if(attachmentArticleId == null) {
        articleRepository.insertArticle(bitmapToSave, imageQuality)
      } else articleRepository.insertArticleImage(bitmapToSave, attachmentArticleId, imageQuality)
      stopWatch.logElapsed("AddArticleViewModel", "Save article time")
    }
  }

  private fun nextImage(){
    processedBitmap = null
    when(processingState){
      is ProcessingState.Album -> nextAlbumUri()
      is ProcessingState.Camera -> launchCameraPermissions = Event(Unit)
    }
  }

  fun error(@StringRes msg: Int) {
    errorState = Event(ErrorState.ProcessingError(msg))
    nextImage()
  }

  private fun nextAlbumUri() {
    val albumState = processingState as ProcessingState.Album
    val nextProcessingIndex = albumState.processingImageIndex + 1
    if(nextProcessingIndex > albumState.imageUriStrings.lastIndex){
      navigationEventState = Event(NavigationState.Back)
      return
    }
    processingState = albumState.copy(processingImageIndex = nextProcessingIndex)
    val imageUri = Uri.parse(albumState.imageUriStrings[nextProcessingIndex])
    if(imageUri == null) {
      error(R.string.image_not_found)
      nextAlbumUri()
    } else {
      processImage(imageUri)
    }
  }

  private fun processImage(imageUri: Uri) {
    imageRotationIndex = 0
    rotation = rotations[imageRotationIndex]
    processing = true
    viewModelScope.launch(Dispatchers.Default) {
      try {
        segmentedImage.process(getApplication(), imageUri) { success ->
          when(success){
            SegmentedImage.ProcessSuccess.SUCCESS -> {
              if(segmentedImage.subjectsFound) refreshProcessedBitmap()
              else error(R.string.no_subject_found)
            }
            SegmentedImage.ProcessSuccess.FAILURE_MLKIT_MODULE_WAITING_TO_DOWNLOAD -> {
              error(R.string.configuring_try_again_soon)
            }
            SegmentedImage.ProcessSuccess.FAILURE_IMAGE_NOT_FOUND -> {
              error(R.string.image_not_found)
            }
            SegmentedImage.ProcessSuccess.FAILURE_IMAGE_NOT_RECOGNIZED -> {
              error(R.string.image_not_recognized)
            }
          }
        }
      } catch(e: IOException) {
        Log.e("processImage()", "ML Kit failed to open image - ${e.message}")
        error(R.string.image_not_found)
      }
    }
  }

  private fun refreshProcessedBitmap() {
    // MutableState utilizes the function 'equals()' to determine if it should emit a value or not
    // In aim of better performance, segmentedImage.subjectBitmap is likely to be the same Bitmap
    // reference but edited in-place. This causes 'equals()' to return true because the reference
    // is the same. Setting the value of the processed Bitmap to anything else before setting it to
    // our desired value forces it to actually emit our desired value in all circumstances.
    processedBitmap = null
    processedBitmap = segmentedImage.subjectBitmap
    processing = false
  }

  private fun nextSubject() {
    if(segmentedImage.subjectIndex == (segmentedImage.subjectCount - 1)) {
      nextImage()
    } else {
      segmentedImage.nextSubject()
      refreshProcessedBitmap()
    }
  }

  fun onCameraPermissionsNeverAskAgain() {
    dialogState = DialogState.CameraPermissions
  }
  fun onCameraPermissionsDenied() {
    errorState = Event(ErrorState.PermissionsDenied)
    navigationEventState = Event(NavigationState.Back)
  }

  fun onDismissCameraPermissionsAlert() {
    dialogState = DialogState.None
    navigationEventState = Event(NavigationState.Back)
  }
  fun onConfirmCameraPermissionsAlert() {
    dialogState = DialogState.None
    navigationEventState = Event(NavigationState.SystemAppSettings)
  }

  fun onCameraPermissionsGranted() {
    val cameraState = processingState as ProcessingState.Camera
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
      navigationEventState = Event(NavigationState.Back)
    } else {
      processingState = cameraState.copy(inProgressPhotoUri = tmpUri)
      navigationEventState = Event(NavigationState.TakePicture(tmpUri))
    }
  }

  fun onCameraPictureTaken(success: Boolean) {
    val cameraState = processingState as ProcessingState.Camera
    if(success){
      if(cameraState.inProgressPhotoUri != null){
        processImage(cameraState.inProgressPhotoUri)
      } else {
        Log.e(TAG, "Temp camera picture URI was null after picture was taken")
        errorState = Event(ErrorState.PhotoLost)
        launchCameraPermissions = Event(Unit)
      }
      processingState = cameraState.copy(inProgressPhotoUri = null)
    } else {
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
        val contentResolver = getApplication<Application>().contentResolver
        cameraState.inProgressPhotoUri?.let { contentResolver.delete(it, null, null) }
        processingState = cameraState.copy(inProgressPhotoUri = null)
      }
//      navigationEventState = Event(NavigationState.Back)
    }
  }
}