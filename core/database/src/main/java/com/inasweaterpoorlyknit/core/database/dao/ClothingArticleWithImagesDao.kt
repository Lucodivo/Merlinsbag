package com.inasweaterpoorlyknit.core.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.inasweaterpoorlyknit.core.database.model.ClothingArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ClothingArticleImageEntity

data class ClothingArticleWithImagesEntity(
  @Embedded val clothingArticleEntity: ClothingArticleEntity,
  @Relation(parentColumn = "id", entityColumn = "clothing_article_id")
  val images: List<ClothingArticleImageEntity>
)

@Dao
interface ClothingArticleWithImagesDao {
  @Insert
  fun insertClothingArticles(vararg clothingArticleEntity: ClothingArticleEntity)
  @Update
  fun updateClothingArticle(clothingArticleEntity: ClothingArticleEntity)

  @Insert
  fun insertClothingArticleImages(vararg clothingArticleImageEntity: ClothingArticleImageEntity)

  // NOTE: @Transaction are necessary when using @Relation entities with the @Relation annotation
  @Transaction
  @Query("""SELECT * FROM clothing_articles
              WHERE clothing_articles.id = :clothingArticleId """)
  fun getClothingArticleWithImages(clothingArticleId: String): LiveData<ClothingArticleWithImagesEntity>

  @Transaction
  @Query("""SELECT * FROM clothing_articles 
                ORDER BY clothing_articles.modified DESC""")
  fun getAllClothingArticlesWithImages(): LiveData<List<ClothingArticleWithImagesEntity>>

  @Transaction
  fun insertClothingArticle(imageUri: String, thumbnailUri: String) {
    val clothingArticle = ClothingArticleEntity()
    val clothingArticleImage = ClothingArticleImageEntity(clothingArticleId = clothingArticle.id, uri = imageUri, thumbnailUri = thumbnailUri)
    insertClothingArticles(clothingArticle)
    insertClothingArticleImages(clothingArticleImage)
  }
}
