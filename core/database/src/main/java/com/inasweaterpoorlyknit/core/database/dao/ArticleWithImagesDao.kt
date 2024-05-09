package com.inasweaterpoorlyknit.core.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.inasweaterpoorlyknit.core.database.model.ArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ArticleImageEntity

data class ArticleWithImagesEntity(
  @Embedded val articleEntity: ArticleEntity,
  @Relation(parentColumn = "id", entityColumn = "article_id")
  val images: List<ArticleImageEntity>
)

@Dao
interface ArticleWithImagesDao {
  @Insert
  fun insertArticles(vararg articleEntity: ArticleEntity)
  @Update
  fun updateArticle(articleEntity: ArticleEntity)

  @Insert
  fun insertArticleImages(vararg articleImageEntity: ArticleImageEntity)

  // NOTE: @Transaction are necessary when using @Relation entities with the @Relation annotation
  @Transaction
  @Query(
    """SELECT * FROM article
              WHERE article.id = :articleId """
  )
  fun getArticleWithImages(articleId: String): LiveData<ArticleWithImagesEntity>

  @Transaction
  @Query("""SELECT * FROM article 
                ORDER BY article.modified DESC""")
  fun getAllArticlesWithImages(): LiveData<List<ArticleWithImagesEntity>>

  @Transaction
  fun insertArticle(imageUri: String, thumbnailUri: String) {
    val article = ArticleEntity()
    val articleImage = ArticleImageEntity(articleId = article.id, uri = imageUri, thumbnailUri = thumbnailUri)
    insertArticles(article)
    insertArticleImages(articleImage)
  }
}
