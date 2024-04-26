package com.inasweaterpoorlyknit.inknit.database.model

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import java.util.UUID

@Entity(
    tableName = "clothing_articles",
)
data class ClothingArticleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    // TODO: Categories
)

@Entity(
    tableName = "clothing_article_images",
    indices = [Index(value = ["clothing_article_id"])]
)
data class ClothingArticleImageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "clothing_article_id") val clothingArticleId: String,
    @ColumnInfo(name = "uri") val uri: String,
    @ColumnInfo(name = "thumbnail_uri") val thumbnailUri: String,
    // TODO: image rank
)

data class ClothingArticleWithImagesEntity(
    @Embedded val clothingArticleEntity: ClothingArticleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "clothing_article_id"
    )
    val images: List<ClothingArticleImageEntity>
)

@Dao
interface ClothingArticleWithImagesDao {
    @Query("SELECT * FROM clothing_articles")
    fun getAllClothingArticles(): List<ClothingArticleEntity>
    @Insert
    fun insertClothingArticles(vararg clothingArticleEntity: ClothingArticleEntity)
    @Delete
    fun deleteClothingArticle(clothingArticleEntity: ClothingArticleEntity)
    @Update
    fun updateClothingArticle(clothingArticleEntity: ClothingArticleEntity)

    @Query("""SELECT * FROM clothing_article_images
              WHERE clothing_article_images.clothing_article_id = :clothingArticleId""")
    fun getClothingArticleImages(clothingArticleId: String): ClothingArticleImageEntity
    @Insert
    fun insertClothingArticleImages(vararg clothingArticleImageEntity: ClothingArticleImageEntity)

    // NOTE: @Transaction are necessary when using @Relation entities with the @Relation annotation
    @Transaction
    @Query("""SELECT * FROM clothing_articles
              WHERE clothing_articles.id = :clothingArticleId """)
    fun getClothingArticleWithImages(clothingArticleId: String): ClothingArticleWithImagesEntity

    @Transaction
    @Query("""SELECT * FROM clothing_articles
              WHERE clothing_articles.id = :clothingArticleId """)
    fun getClothingArticleWithImagesLive(clothingArticleId: String): LiveData<ClothingArticleWithImagesEntity>

    @Transaction
    @Query("""SELECT * FROM clothing_articles""")
    fun getAllClothingArticlesWithImages(): LiveData<List<ClothingArticleWithImagesEntity>>

    @Transaction
    fun insertClothingArticle(imageUri: String, thumbnailUri: String) {
        val clothingArticle = ClothingArticleEntity()
        val clothingArticleImage = ClothingArticleImageEntity(clothingArticleId = clothingArticle.id, uri = imageUri, thumbnailUri = thumbnailUri)
        insertClothingArticles(clothingArticle)
        insertClothingArticleImages(clothingArticleImage)
    }
}

@Database(entities = [ClothingArticleEntity::class, ClothingArticleImageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clothingArticleWithImagesDao(): ClothingArticleWithImagesDao
}