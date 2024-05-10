package com.inasweaterpoorlyknit.core.database.dao

import androidx.lifecycle.LiveData
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

data class ArticleWithImages(
  @ColumnInfo("article_id") val articleId: String,
  @Relation(parentColumn = "article_id", entityColumn = "article_id")
  val images: List<ArticleImageEntity>
)

@Dao
interface ArticleDao {
  @Insert
  fun insertArticles(vararg articleEntity: ArticleEntity)
  @Update
  fun updateArticle(articleEntity: ArticleEntity)

  @Insert
  fun insertArticleImages(vararg articleImageEntity: ArticleImageEntity)

  // NOTE: @Transaction are necessary when using @Relation entities with the @Relation annotation
  @Transaction
  @Query(
    """SELECT article.id as article_id FROM article
       WHERE article.id = :articleId """
  )
  fun getArticleWithImages(articleId: String): LiveData<ArticleWithImages>

  @Transaction
  @Query("""SELECT article.id as article_id FROM article 
            ORDER BY article.modified DESC""")
  fun getAllArticlesWithImages(): Flow<List<ArticleWithImages>>

  @Transaction
  fun insertArticle(imageUri: String, thumbnailUri: String) {
    val article = ArticleEntity()
    val articleImage = ArticleImageEntity(articleId = article.id, uri = imageUri, thumbUri = thumbnailUri)
    insertArticles(article)
    insertArticleImages(articleImage)
  }
}
