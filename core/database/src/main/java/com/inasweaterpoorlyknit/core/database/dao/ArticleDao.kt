package com.inasweaterpoorlyknit.core.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.inasweaterpoorlyknit.core.database.model.ArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ArticleImageEntity
import kotlinx.coroutines.flow.Flow

data class ArticleThumbnailPath(
  @ColumnInfo("article_id") val articleId: String,
  @ColumnInfo("thumb_uri") val uriThumb: String,
)

data class ImagePaths(
  @ColumnInfo("uri") val uri: String,
  @ColumnInfo("thumb_uri") val uriThumb: String,
)

data class FullImagePath(
  @ColumnInfo("uri") val uri: String,
)

data class ThumbnailPath(
  @ColumnInfo("thumb_uri") val uri: String,
)

data class ArticleWithThumbnails(
  @ColumnInfo("article_id") val articleId: String,
  @Relation(parentColumn = "article_id", entityColumn = "article_id", entity = ArticleImageEntity::class)
  val thumbnailPaths: List<ThumbnailPath>
)

data class ArticleWithImages(
  @ColumnInfo("article_id") val articleId: String,
  @Relation(parentColumn = "article_id", entityColumn = "article_id", entity = ArticleImageEntity::class)
  val imagePaths: List<ImagePaths>
)

data class ArticleWithFullImages(
  @ColumnInfo("article_id") val articleId: String,
  @Relation(parentColumn = "article_id", entityColumn = "article_id", entity = ArticleImageEntity::class)
  val fullImagePaths: List<FullImagePath>
)

@Dao
interface ArticleDao {
//region Good Queries
  @Insert fun insertArticles(vararg articleEntity: ArticleEntity)
  @Update fun updateArticle(articleEntity: ArticleEntity)
  @Insert fun insertArticleImages(vararg articleImageEntity: ArticleImageEntity)

  @Query("""DELETE FROM article WHERE id IN (:articleIds)""")
  fun deleteArticles(articleIds: List<String>)

  @Transaction
  fun insertArticle(imageUri: String, thumbnailUri: String) {
    val article = ArticleEntity()
    val articleImage = ArticleImageEntity(articleId = article.id, uri = imageUri, thumbUri = thumbnailUri)
    insertArticles(article)
    insertArticleImages(articleImage)
  }
//endregion

//region
  @Transaction
  @Query("""SELECT article.id as article_id FROM article 
            ORDER BY article.modified DESC""")
  fun getAllArticlesWithThumbnails(): Flow<List<ArticleWithThumbnails>>

  @Transaction
  @Query("""SELECT article.id as article_id FROM article 
            ORDER BY article.modified DESC""")
  fun getAllArticlesWithFullImages(): Flow<List<ArticleWithFullImages>>

  @Transaction
  @Query("""SELECT article.id as article_id FROM article
            WHERE article.id IN (:articleIds)
            ORDER BY article.modified DESC""")
  fun getArticlesWithThumbnails(articleIds: List<String>): Flow<List<ArticleWithThumbnails>>

  @Transaction
  @Query("""SELECT article.id as article_id FROM article
            WHERE article.id IN (:articleIds)
            ORDER BY article.modified DESC""")
  fun getArticlesWithImages(articleIds: List<String>): Flow<List<ArticleWithImages>>
//endregion
}
