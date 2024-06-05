package com.inasweaterpoorlyknit.core.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.inasweaterpoorlyknit.core.common.articleFilesDir
import com.inasweaterpoorlyknit.core.common.articleFilesDirStr
import com.inasweaterpoorlyknit.core.common.exportFilesDir
import com.inasweaterpoorlyknit.core.common.timestampFileName
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.repository.model.LazyArticleFullImages
import com.inasweaterpoorlyknit.core.repository.model.LazyArticleThumbnails
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream

val compressionFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP

class ArticleRepository(
  private val context: Context,
  private val articleDao: ArticleDao,
  private val ensembleDao: EnsembleDao,
) {
  fun insertArticle(imageUri: String, thumbnailUri: String) = articleDao.insertArticle(imageUri, thumbnailUri)
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

  suspend fun exportArticle(articleId: String): Uri {
    val articleFilenames = articleDao.getArticleFilenames(articleId).first()
    val articleFilename = articleFilenames.imagePaths[0].filename
    val articleFilesDir = articleFilesDir(context)
    val articleFile = File(articleFilesDir, articleFilename)
    val exportDir = exportFilesDir(context).apply { mkdirs() }
    val exportFile = File(exportDir, articleFilename)
    articleFile.copyTo(exportFile, true)
    return exportFile.toUri()
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

fun saveBitmap(directory: File, fileName: String, bitmap: Bitmap){
  val imageFile = File(directory, fileName)
  FileOutputStream(imageFile).use { outStream ->
    bitmap.compress(compressionFormat, 100, outStream)
    outStream.flush()
  }
}
