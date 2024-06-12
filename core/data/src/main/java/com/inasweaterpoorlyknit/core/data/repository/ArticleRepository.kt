package com.inasweaterpoorlyknit.core.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.scale
import com.inasweaterpoorlyknit.core.common.timestampFileName
import com.inasweaterpoorlyknit.core.data.model.LazyArticleFullImages
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

val compressionFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP
val exportFormat = Bitmap.CompressFormat.PNG

class ArticleRepository(
  private val context: Context,
  private val articleDao: ArticleDao,
  private val ensembleDao: EnsembleDao,
) {
  // TODO: Remove. Used in testing only.
  fun insertArticle(fullImageUri: String, thumbnailImageUri: String) = articleDao.insertArticle(fullImageUri, thumbnailImageUri)

  fun insertArticle(bitmap: Bitmap) {
    val articleFilesDir = articleFilesDir(context).apply { mkdirs() }
    val thumbnailBitmapToSave = bitmap.toThumbnail()
    val filenameBase = timestampFileName()
    val fullImageFilename = "${filenameBase}_full.webp"
    val thumbnailFilename = "${filenameBase}_thumb.webp"
    saveBitmap(articleFilesDir, fullImageFilename, bitmap)
    saveBitmap(articleFilesDir, thumbnailFilename, thumbnailBitmapToSave)
    articleDao.insertArticle(fullImageFilename, thumbnailFilename)
  }

  suspend fun deleteArticles(articleIds: List<String>) {
    val articleFilesDir = articleFilesDir(context)
    val articleWithImages = articleDao.getArticlesWithImages(articleIds).first()
    articleDao.deleteArticles(articleIds)
    articleWithImages.forEach { articleWithImage ->
      articleWithImage.imagePaths.forEach { articleImage ->
        if(!File(articleFilesDir, articleImage.filename).delete()) Log.e("ArticleRepository", "Failed to delete image ${articleImage.filename}")
        if(!File(articleFilesDir, articleImage.filenameThumb).delete()) Log.e("ArticleRepository", "Failed to delete thumbnail ${articleImage.filenameThumb}")
      }
    }
  }
  suspend fun deleteArticle(articleId: String) = deleteArticles(listOf(articleId))

  suspend fun exportArticle(articleId: String): Uri? {
    val articleFilenames = articleDao.getArticleFilenames(articleId).first()
    val articleFilename = articleFilenames.imagePaths[0].filename
    val articleFilesDir = articleFilesDir(context)
    val articleFile = File(articleFilesDir, articleFilename)

    val bitmapToExport = BitmapFactory.decodeFile(articleFile.path)
    val exportFilname = articleFilename.replace(".webp", ".png")

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
        Log.e("Export Article", "${e.message}\nFailed to copy article image to public directory: ${articleFile.path} -> ${exportFile.path}")
      }
    }

    return if(success) exportUri else null
  }

  fun getAllArticlesWithThumbnails() = articleDao.getAllArticlesWithThumbnails().map { LazyArticleThumbnails(articleFilesDirStr(context), it) }
  fun getAllArticlesWithFullImages() = articleDao.getAllArticlesWithFullImages().map { LazyArticleFullImages(articleFilesDirStr(context), it) }
  fun getArticlesWithFullImages(ensembleId: String?) = if(ensembleId == null) getAllArticlesWithFullImages()
                                                  else ensembleDao.getEnsembleArticleFullImages(ensembleId).map { LazyArticleFullImages(articleFilesDirStr(context), it) }
}

private fun Bitmap.toThumbnail(): Bitmap{
  var bitmapWidth = width
  var bitmapHeight = height
  while (bitmapWidth > 300 || bitmapHeight > 300) {
    bitmapWidth /= 2
    bitmapHeight /= 2
  }
  return scale(bitmapWidth, bitmapHeight)
}

private fun saveBitmap(directory: File, fileName: String, bitmap: Bitmap){
  val imageFile = File(directory, fileName)
  FileOutputStream(imageFile).use { outStream ->
    bitmap.compress(compressionFormat, 100, outStream)
    outStream.flush()
  }
}