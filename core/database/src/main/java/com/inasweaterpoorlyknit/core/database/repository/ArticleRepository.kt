package com.inasweaterpoorlyknit.core.database.repository

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
private fun timestampFileName(): String = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

class ArticleRepository(
  private val context: Context,
  private val articleDao: ArticleDao,
  private val ensembleDao: EnsembleDao
) {
  val articleFilesDir = File(context.filesDir, "articles").apply{ mkdirs() }

  fun getAllArticlesWithImages(): Flow<List<ArticleWithImages>> = articleDao.getAllArticlesWithImages()
  fun getArticleWithImages(id: String): Flow<ArticleWithImages> = articleDao.getArticleWithImages(id)
  fun getArticlesWithImages(ensembleId: String?): Flow<List<ArticleWithImages>> {
    return if(ensembleId == null) getAllArticlesWithImages()
    else ensembleDao.getEnsembleArticleImages(ensembleId)
  }

  fun insertArticle(imageUri: String, thumbnailUri: String): Unit = articleDao.insertArticle(imageUri, thumbnailUri)

  fun insertArticle(bitmap: Bitmap) {
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
      imageFile.toUri().toString(),
      thumbnailFile.toUri().toString()
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
          val imageFile = File(articleFilesDir, articleImage.uri.substringAfterLast("/"))
          val thumbnailFile = File(articleFilesDir, articleImage.thumbUri.substringAfterLast("/"))
          if(!imageFile.delete()) Log.e("ArticleRepository", "Failed to delete image ${articleImage.uri}")
          if(!thumbnailFile.delete()) Log.e("ArticleRepository", "Failed to delete thumbnail ${articleImage.thumbUri}")
        }
      }
    }
  }
}