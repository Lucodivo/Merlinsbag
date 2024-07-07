package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.inasweaterpoorlyknit.core.common.timestampFileName
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(): ViewModel() {
  var takePictureUri: Uri? = null

  fun onTakePicture(context: Context): Uri? {
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

  fun pictureTaken(taken: Boolean, context: Context) {
    if(!taken && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val contentResolver = context.contentResolver
      takePictureUri?.let { contentResolver.delete(it, null, null) }
    }
  }
}