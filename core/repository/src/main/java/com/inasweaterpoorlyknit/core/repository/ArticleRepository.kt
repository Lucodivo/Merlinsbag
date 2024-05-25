package com.inasweaterpoorlyknit.core.repository

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.core.graphics.scale
import com.inasweaterpoorlyknit.core.common.listMap
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.model.ArticleImageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
private fun timestampFileName(): String = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
fun articleFilesDir(context: Context) = File(context.filesDir, "articles")

// TODO: Consider a more efficient way of appending file directory
fun ArticleWithImages.appendDirectory(context: Context): ArticleWithImages {
  val dir = articleFilesDir(context).toString()
  return copy(images = images.map { image ->
    image.copy(
      uri = "$dir/${image.uri}",
      thumbUri = "$dir/${image.thumbUri}"
    )})
}

fun ArticleImageEntity.appendDirectory(context: Context): ArticleImageEntity {
  val dir = articleFilesDir(context).toString()
  return copy(
    uri = "$dir/${uri}",
    thumbUri = "$dir/${thumbUri}"
  )
}

class ArticleRepository(
  private val context: Context,
  private val articleDao: ArticleDao,
  private val ensembleDao: EnsembleDao
) {
  private val articleFilesDir = articleFilesDir(context)

  fun getAllArticlesWithImages(): Flow<List<ArticleWithImages>> {
    return articleDao.getAllArticlesWithImages().listMap { it.appendDirectory(context) }
  }
  fun getArticleWithImages(id: String): Flow<ArticleWithImages> {
    return articleDao.getArticleWithImages(id).map { it.appendDirectory(context) }
  }
  fun getArticlesWithImages(ensembleId: String?): Flow<List<ArticleWithImages>> {
    return if(ensembleId == null) getAllArticlesWithImages()
    else ensembleDao.getEnsembleArticleImages(ensembleId).listMap { it.appendDirectory(context) }
  }

  fun insertArticle(imageUri: String, thumbnailUri: String) = articleDao.insertArticle(imageUri, thumbnailUri)

  fun insertArticle(bitmap: Bitmap) {
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

    // save article full bitmap
    FileOutputStream(imageFile).use { outStream ->
      // Compress the bitmap into a JPEG (you could also use PNG)
      bitmap.compress(compressionFormat, 100, outStream)
      // Flush and close the output stream
      outStream.flush()
    }

    // save article thumbnail bitmap
    FileOutputStream(thumbnailFile).use { outStream ->
      // Compress the bitmap into a JPEG (you could also use PNG)
      thumbnailBitmapToSave.compress(compressionFormat, 100, outStream)
      // Flush and close the output stream
      outStream.flush()
    }
    articleDao.insertArticle(
      imageFilename,
      thumbnailFilename
    )
  }

  fun deleteArticles(articleIds: List<String>) {
    runBlocking {
      val articleWithImages = articleDao.getArticlesWithImages(articleIds).first()
      // delete records from database
      articleDao.deleteArticles(articleIds)
      // delete associated images
      articleWithImages.forEach { articleWithImage ->
        articleWithImage.images.forEach { articleImage ->
          val imageFile = File(articleFilesDir, articleImage.uri)
          val thumbnailFile = File(articleFilesDir, articleImage.thumbUri)
          if(!imageFile.delete()) Log.e("ArticleRepository", "Failed to delete image ${articleImage.uri}")
          if(!thumbnailFile.delete()) Log.e("ArticleRepository", "Failed to delete thumbnail ${articleImage.thumbUri}")
        }
      }
    }
  }
}