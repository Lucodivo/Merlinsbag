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
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithFullImages
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream

interface LazyUriStrings {
  companion object {
    val Empty = object : LazyUriStrings {
      override val size get() = 0
      override fun getUriString(index: Int): String = ""
    }
  }
  val size: Int
  fun isEmpty() = size == 0
  fun isNotEmpty() = size != 0
  fun getUriString(index: Int): String
}

class LazyArticleThumbnails(
  val directory: String,
  private var articleThumbnailPaths: List<ArticleWithThumbnails>
): LazyUriStrings {
  override val size get() = articleThumbnailPaths.size
  fun getArticleId(index: Int) = articleThumbnailPaths[index].articleId
  override fun getUriString(index: Int): String = "$directory/${articleThumbnailPaths[index].thumbnailPaths[0].uri}"
  fun filter(keep: (ArticleWithThumbnails) -> Boolean) = LazyArticleThumbnails(directory, articleThumbnailPaths.filter(keep))
  fun articleIds() = articleThumbnailPaths.map { it.articleId }
}

class LazyArticleFullImages(
  val directory: String,
  private var articleFullImagePaths: List<ArticleWithFullImages>
): LazyUriStrings {
  override val size get() = articleFullImagePaths.size
  fun getArticleId(index: Int) = articleFullImagePaths[index].articleId
  override fun getUriString(index: Int): String = "$directory/${articleFullImagePaths[index].fullImagePaths[0].uri}"
  fun articleIds() = articleFullImagePaths.map { it.articleId }
}

class ArticleRepository(
  private val context: Context,
  private val articleDao: ArticleDao,
  private val ensembleDao: EnsembleDao,
) {
  fun insertArticle(imageUri: String, thumbnailUri: String) = articleDao.insertArticle(imageUri, thumbnailUri)
  fun insertArticle(bitmap: Bitmap) {
    val articleFilesDir = articleFilesDir(context)
    articleFilesDir.mkdirs()
    var bitmapWidth = bitmap.width
    var bitmapHeight = bitmap.height
    while (bitmapWidth > 300 || bitmapHeight > 300) {
      bitmapWidth /= 2
      bitmapHeight /= 2
    }
    val thumbnailBitmapToSave = bitmap.scale(bitmapWidth, bitmapHeight)

    val filenameBase = timestampFileName()
    val imageFilename = "${filenameBase}_full.webp"
    val thumbnailFilename = "${filenameBase}_thumb.webp"
    val imageFile = File(articleFilesDir, imageFilename)
    val thumbnailFile = File(articleFilesDir, thumbnailFilename)

    @Suppress("DEPRECATION")
    val compressionFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP

    FileOutputStream(imageFile).use { outStream ->
      bitmap.compress(compressionFormat, 100, outStream)
      outStream.flush()
    }
    FileOutputStream(thumbnailFile).use { outStream ->
      thumbnailBitmapToSave.compress(compressionFormat, 100, outStream)
      outStream.flush()
    }

    articleDao.insertArticle(
      imageFilename,
      thumbnailFilename
    )
  }

  fun deleteArticles(articleIds: List<String>) {
    val articleFilesDir = articleFilesDir(context)
    runBlocking {
      val articleWithImages = articleDao.getArticlesWithImages(articleIds).first()
      // delete records from database
      articleDao.deleteArticles(articleIds)
      // delete associated images
      articleWithImages.forEach { articleWithImage ->
        articleWithImage.imagePaths.forEach { articleImage ->
          val imageFile = File(articleFilesDir, articleImage.uri)
          val thumbnailFile = File(articleFilesDir, articleImage.uriThumb)
          if(!imageFile.delete()) Log.e("ArticleRepository", "Failed to delete image ${articleImage.uri}")
          if(!thumbnailFile.delete()) Log.e("ArticleRepository", "Failed to delete thumbnail ${articleImage.uriThumb}")
        }
      }
    }
  }

  fun getAllArticlesWithThumbnails() = articleDao.getAllArticlesWithThumbnails().map { LazyArticleThumbnails(articleFilesDirStr(context), it) }
  fun getAllArticlesWithFullImages() = articleDao.getAllArticlesWithFullImages().map { LazyArticleFullImages(articleFilesDirStr(context), it) }
  fun getArticlesWithFullImages(ensembleId: String?) = if(ensembleId == null) getAllArticlesWithFullImages()
                                                  else ensembleDao.getEnsembleArticleFullImages(ensembleId).map { LazyArticleFullImages(articleFilesDirStr(context), it) }
}