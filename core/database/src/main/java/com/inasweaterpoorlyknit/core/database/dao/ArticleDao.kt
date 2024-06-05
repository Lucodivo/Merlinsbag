package com.inasweaterpoorlyknit.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.inasweaterpoorlyknit.core.database.model.ArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ArticleImageEntity
import com.inasweaterpoorlyknit.core.database.model.ArticleWithFullImages
import com.inasweaterpoorlyknit.core.database.model.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
  @Insert fun insertArticles(vararg articleEntity: ArticleEntity)
  @Insert fun insertArticleImages(vararg articleImageEntity: ArticleImageEntity)
  @Update fun updateArticle(articleEntity: ArticleEntity)

  @Query("""DELETE FROM article WHERE id IN (:articleIds)""")
  fun deleteArticles(articleIds: List<String>)

  @Query("""DELETE FROM article""")
  fun deleteAllArticles()

  @Transaction
  fun insertArticle(imageUri: String, thumbnailUri: String) {
    val article = ArticleEntity()
    val articleImage = ArticleImageEntity(articleId = article.id, uri = imageUri, thumbUri = thumbnailUri)
    insertArticles(article)
    insertArticleImages(articleImage)
  }

  @Transaction @Query(
    """SELECT article.id as article_id FROM article 
        ORDER BY article.modified DESC""")
  fun getAllArticlesWithThumbnails(): Flow<List<ArticleWithThumbnails>>

  @Transaction @Query(
    """SELECT article.id as article_id FROM article 
        ORDER BY article.modified DESC""")
  fun getAllArticlesWithFullImages(): Flow<List<ArticleWithFullImages>>

  @Transaction @Query(
    """SELECT article.id as article_id FROM article
        WHERE article.id IN (:articleIds)
        ORDER BY article.modified DESC""")
  fun getArticlesWithThumbnails(articleIds: List<String>): Flow<List<ArticleWithThumbnails>>

  @Transaction @Query(
    """SELECT article.id as article_id FROM article
        WHERE article.id IN (:articleIds)
        ORDER BY article.modified DESC""")
  fun getArticlesWithImages(articleIds: List<String>): Flow<List<ArticleWithImages>>
}
