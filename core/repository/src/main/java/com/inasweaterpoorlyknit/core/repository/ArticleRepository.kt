package com.inasweaterpoorlyknit.core.repository

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.core.graphics.scale
import com.inasweaterpoorlyknit.core.common.articleFilesDir
import com.inasweaterpoorlyknit.core.common.articleFilesDirStr
import com.inasweaterpoorlyknit.core.common.timestampFileName
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.repository.model.LazyArticleFullImages
import com.inasweaterpoorlyknit.core.repository.model.LazyArticleThumbnails
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
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

  fun deleteArticles(articleIds: List<String>) {
    val articleFilesDir = articleFilesDir(context)
    runBlocking {
      val articleWithImages = articleDao.getArticlesWithImages(articleIds).first()
      articleDao.deleteArticles(articleIds)
      articleWithImages.forEach { articleWithImage ->
        articleWithImage.imagePaths.forEach { articleImage ->
          if(!File(articleFilesDir, articleImage.uri).delete()) Log.e("ArticleRepository", "Failed to delete image ${articleImage.uri}")
          if(!File(articleFilesDir, articleImage.uriThumb).delete()) Log.e("ArticleRepository", "Failed to delete thumbnail ${articleImage.uriThumb}")
        }
      }
    }
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
