package com.inasweaterpoorlyknit.core.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.scale
import com.inasweaterpoorlyknit.core.common.timestampFileName
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.model.LazyArticlesWithImages
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.model.ArticleImageEntity
import com.inasweaterpoorlyknit.core.database.model.ImageFilenames
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

val compressionFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
val compressionQualityFull = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) 100 else 99
const val compressionQualityThumb = 70
val exportFormat = Bitmap.CompressFormat.PNG

class ArticleRepository(
  private val context: Context,
  private val articleDao: ArticleDao,
  private val ensembleDao: EnsembleDao,
) {
  // TODO: Remove. Used in testing only.
  fun insertArticle(fullImageUri: String, thumbnailImageUri: String) = articleDao.insertArticle(fullImageUri, thumbnailImageUri)

  private fun saveBitmaps(bitmap: Bitmap): ImageFilenames {
    val articleFilesDir = articleFilesDir(context).apply { mkdirs() }
    val thumbnailBitmapToSave = bitmap.toThumbnail()
    val filenameBase = timestampFileName()
    val imageFilenames = ImageFilenames(
      filename = "${filenameBase}_full.webp",
      filenameThumb = "${filenameBase}_thumb.webp",
    )
    saveBitmap(articleFilesDir, imageFilenames.filename, bitmap, compressionQualityFull)
    saveBitmap(articleFilesDir, imageFilenames.filenameThumb, thumbnailBitmapToSave, compressionQualityThumb)
    return imageFilenames
  }

  fun insertArticle(bitmap: Bitmap) {
    val filenames = saveBitmaps(bitmap)
    articleDao.insertArticle(filenames.filename, filenames.filenameThumb)
  }

  fun insertArticleImage(bitmap: Bitmap, articleId: String){
    val filenames = saveBitmaps(bitmap)
    articleDao.insertArticleImages(
      ArticleImageEntity(
        articleId = articleId,
        filename = filenames.filename,
        filenameThumb = filenames.filenameThumb,
      )
    )
  }

  fun deleteArticleImages(articleImageFilenamesThumb: List<String>) = articleDao.deleteArticleImages(articleImageFilenamesThumb)

  suspend fun deleteArticles(articleIds: List<String>) {
    val articleFilesDir = articleFilesDir(context)
    val articlesWithImages = articleDao.getArticlesWithImages(articleIds).first()
    articleDao.deleteArticles(articleIds)
    articlesWithImages.forEach { articleWithImages ->
      articleWithImages.imagePaths.forEach { articleImage ->
        if(!File(articleFilesDir, articleImage.filename).delete()) Log.e("ArticleRepository", "Failed to delete image ${articleImage.filename}")
        if(!File(articleFilesDir, articleImage.filenameThumb).delete()) Log.e("ArticleRepository", "Failed to delete thumbnail ${articleImage.filenameThumb}")
      }
    }
  }
  suspend fun deleteArticle(articleId: String) = deleteArticles(listOf(articleId))

  fun exportImage(imageUri: String): Uri? {
    val bitmapToExport = BitmapFactory.decodeFile(imageUri)

    val exportFilname = imageUri.replace(".webp", ".png").substringAfterLast("/")

    var exportUri: Uri? = null
    var success = false
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val resolver = context.contentResolver
      val contentValues = ContentValues()
      contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, exportFilname)
      contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
      contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, exportDirGreaterEqualQ)
      // TODO: MediaStore.MediaColumns.IN_PROGRESS
      exportUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
      exportUri?.let { uri ->
        resolver.openOutputStream(uri)?.let { fileOutputStream ->
          bitmapToExport.compress(exportFormat, 100, fileOutputStream)
          fileOutputStream.flush()
          fileOutputStream.close()
          success = true
        }
      }
    } else {
      val exportFile = File(exportDirLessThanQ, exportFilname)
      try {
        FileOutputStream(exportFile).let { fileOutputStream ->
          bitmapToExport.compress(exportFormat, 100, fileOutputStream)
          fileOutputStream.flush()
          fileOutputStream.close()
        }
        MediaScannerConnection.scanFile(context, arrayOf(exportFile.toString()), arrayOf("image/png"), null)
        exportUri = Uri.fromFile(exportFile)
        success = true
      } catch(e: IOException){
        Log.e("Export Article", "${e.message}\nFailed to copy article image to public directory: $imageUri -> ${exportFile.path}")
      }
    }

    return if(success) exportUri else null
  }

  fun getCountArticles() = articleDao.getCountArticles()
  fun getCountArticleImages() = articleDao.getCountArticleImages()
  fun getMostPopularArticlesImageCount(count: Int): Flow<LazyUriStrings> = articleDao.getMostPopularArticleThumbnails(count).map{ LazyArticleThumbnails(articleFilesDirStr(context), it) }
  fun getAllArticlesWithThumbnails() = articleDao.getAllArticlesWithThumbnails().map { LazyArticleThumbnails(articleFilesDirStr(context), it) }
  fun getArticlesWithImages(ensembleId: String? = null): Flow<LazyArticlesWithImages> =
      if(ensembleId == null) { articleDao.getAllArticlesWithImages() }
      else { ensembleDao.getEnsembleArticleWithImages(ensembleId) }
          .map { LazyArticlesWithImages(articleFilesDirStr(context), it) }
}

private fun Bitmap.toThumbnail(): Bitmap{
  val maxWidth = 300f
  val maxHeight = 450f
  val desiredWidthScale = maxWidth / width.toFloat()
  val desiredHeightScale = maxHeight / height.toFloat()
  val scale = min(desiredWidthScale, desiredHeightScale)
  return if(scale < 1.0f){
    val desiredWidth = (width * scale).toInt()
    val desiredHeight = (height * scale).toInt()
    scale(desiredWidth, desiredHeight)
  } else this
}

private fun saveBitmap(
    directory: File,
    fileName: String,
    bitmap: Bitmap,
    compressionQuality: Int,
){
  val imageFile = File(directory, fileName)
  FileOutputStream(imageFile).use { outStream ->
    bitmap.compress(compressionFormat, compressionQuality, outStream)
    outStream.flush()
  }
}
