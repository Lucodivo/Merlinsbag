package com.inasweaterpoorlyknit.core.image

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.scale
import com.inasweaterpoorlyknit.core.common.timestampFileName
import com.inasweaterpoorlyknit.core.model.ImageFilenames
import com.inasweaterpoorlyknit.core.model.proto.preference.ImageQuality
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

private const val maxThumbnailWidth = 300f
private const val maxThumbnailHeight = 450f

fun saveBitmap(
    directory: File,
    fileName: String,
    bitmap: Bitmap,
    compressionFormat: Bitmap.CompressFormat,
    compressionQuality: Int,
){
    val imageFile = File(directory, fileName)
    FileOutputStream(imageFile).use { outStream ->
        bitmap.compress(compressionFormat, compressionQuality, outStream)
        outStream.flush()
    }
}

fun saveBitmaps(context: Context, bitmap: Bitmap, imageQuality: ImageQuality): ImageFilenames {
    val articleFilesDir = articleFilesDir(context).apply { mkdirs() }
    val thumbnailBitmapToSave = bitmap.toThumbnail()
    val filenameBase = timestampFileName()
    val imageFilenames = object: ImageFilenames{
        override val filename = "${filenameBase}_full.webp"
        override val filenameThumb = "${filenameBase}_thumb.webp"
    }
    saveBitmap(articleFilesDir, imageFilenames.filename, bitmap, imageQuality.compressionFormat(), imageQuality.compressionQuality())
    saveBitmap(articleFilesDir, imageFilenames.filenameThumb, thumbnailBitmapToSave, compressionFormatThumb, compressionQualityThumb)
    return imageFilenames
}

fun Bitmap.toThumbnail(): Bitmap {
    val desiredWidthScale = maxThumbnailWidth / width.toFloat()
    val desiredHeightScale = maxThumbnailHeight / height.toFloat()
    val scale = min(desiredWidthScale, desiredHeightScale)
    return if(scale < 1.0f){
        val desiredWidth = (width * scale).toInt()
        val desiredHeight = (height * scale).toInt()
        scale(desiredWidth, desiredHeight)
    } else this
}
