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
  @Transaction
  fun insertArticle(filename: String, filenameThumb: String) {
    val created = generateTime()
    val article = ArticleEntity(id = generateId(), created = created, modified = created)
    val articleImage = ArticleImageEntity(id = generateId(), articleId = article.id, filename = filename, filenameThumb = filenameThumb)
    insertArticles(article)
    insertArticleImages(articleImage)
  }

  @Query("""UPDATE article
            SET modified = :modified
            WHERE id IN (:articleIds) """)
  fun updateArticlesModified(modified: Long = generateTime(), articleIds: List<String>)

  @Update fun updateArticle(articleEntity: ArticleEntity)

  @Query("""DELETE FROM article WHERE id IN (:articleIds)""")
  fun deleteArticles(articleIds: List<String>)

  @Query("""DELETE FROM article""")
  fun deleteAllArticles()

  @Query("""DELETE FROM article_image WHERE filename_thumb IN (:filenamesThumb)""")
  fun deleteArticleImages(filenamesThumb: List<String>)

  @Transaction @Query(
    """SELECT article.id as article_id FROM article 
        ORDER BY article.created DESC""")
  fun getAllArticlesWithThumbnails(): Flow<List<ArticleWithThumbnails>>

  @Transaction @Query(
    """SELECT article.id as article_id FROM article 
        ORDER BY article.created DESC""")
  fun getAllArticlesWithFullImages(): Flow<List<ArticleWithFullImages>>

  @Transaction @Query(
    """SELECT article.id as article_id FROM article
        ORDER BY article.created DESC""")
  fun getAllArticlesWithImages(): Flow<List<ArticleWithImages>>

  @Transaction @Query(
    """SELECT article.id as article_id FROM article
        WHERE article.id IN (:articleIds)
        ORDER BY article.created DESC""")
  fun getArticlesWithThumbnails(articleIds: List<String>): Flow<List<ArticleWithThumbnails>>

  @Transaction @Query(
    """SELECT article.id as article_id FROM article
        WHERE article.id IN (:articleIds)
        ORDER BY article.created DESC""")
  fun getArticlesWithImages(articleIds: List<String>): Flow<List<ArticleWithImages>>

  @Transaction @Query(
    """SELECT article.id as article_id FROM article
        WHERE article.id = :articleId
        ORDER BY article.created DESC""")
  fun getArticleFilenames(articleId: String): Flow<ArticleWithImages>

  @Query("""SELECT COUNT(id) FROM article""")
  fun getCountArticles(): Flow<Int>
}
