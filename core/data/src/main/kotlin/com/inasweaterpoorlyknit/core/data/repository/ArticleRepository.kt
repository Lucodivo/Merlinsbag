package com.inasweaterpoorlyknit.core.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.model.LazyArticlesWithImages
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.entity.ArticleImageEntity
import com.inasweaterpoorlyknit.core.image.articleFilesDir
import com.inasweaterpoorlyknit.core.image.articleFilesDirStr
import com.inasweaterpoorlyknit.core.image.exportImage
import com.inasweaterpoorlyknit.core.image.saveBitmaps
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.core.model.proto.preference.ImageQuality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

class ArticleRepository(
  private val context: Context,
  private val articleDao: ArticleDao,
  private val ensembleDao: EnsembleDao,
) {
  // Used in testing only.
  fun insertArticle(fullImageUri: String, thumbnailImageUri: String) =
    articleDao.insertArticle(fullImageUri, thumbnailImageUri)
  fun insertArticle(bitmap: Bitmap, imageQuality: ImageQuality) {
    val filenames = saveBitmaps(context, bitmap, imageQuality)
    articleDao.insertArticle(filenames.filename, filenames.filenameThumb)
  }
  fun insertArticleImage(bitmap: Bitmap, articleId: String, imageQuality: ImageQuality){
    val filenames = saveBitmaps(context, bitmap, imageQuality)
    articleDao.insertArticleImages(
      ArticleImageEntity(
        articleId = articleId,
        filename = filenames.filename,
        filenameThumb = filenames.filenameThumb,
      )
    )
  }

  fun exportImage(imageUri: String): Uri? = exportImage(context = context, imageUri = imageUri)

  fun deleteArticleImages(articleImageFilenamesThumb: List<String>) =
    articleDao.deleteArticleImages(articleImageFilenamesThumb)
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

  fun getCountArticles(): Flow<Int> = articleDao.getCountArticles()
  fun getCountArticleImages(): Flow<Int> = articleDao.getCountArticleImages()
  fun getMostPopularArticlesImageCount(count: Int): Flow<LazyUriStrings> =
    articleDao.getMostPopularArticleThumbnails(count).map{
      LazyArticleThumbnails(articleFilesDirStr(context), it)
    }
  fun getAllArticlesWithThumbnails(): Flow<LazyArticleThumbnails> =
    articleDao.getAllArticlesWithThumbnails().map {
      LazyArticleThumbnails(articleFilesDirStr(context), it)
    }
  fun getArticlesWithImages(ensembleId: String? = null): Flow<LazyArticlesWithImages> =
      if(ensembleId == null) { articleDao.getAllArticlesWithImages() }
      else { ensembleDao.getEnsembleArticleWithImages(ensembleId) }
          .map { LazyArticlesWithImages(articleFilesDirStr(context), it) }
}