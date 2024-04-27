package com.inasweaterpoorlyknit.core.database.model

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
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
import java.util.Date
import java.util.UUID

@Entity(tableName = "clothing_articles")
data class ClothingArticleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "created") val createdEpoch: Long = Date().time,
    @ColumnInfo(name = "modified") val modifiedEpoch: Long = createdEpoch,
    // TODO: Categories
){
    val createdDate: Date
        get() = Date(createdEpoch)
    val modifiedDate: Date
        get() = Date(modifiedEpoch)
}

@Entity(tableName = "outfits")
data class OutfitEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "created") val createdEpoch: Long = Date().time,
    @ColumnInfo(name = "modified") val modifiedEpoch: Long = createdEpoch,
)

// Outfits & Clothing Articles join table
@Entity(tableName = "outfit_articles",
        primaryKeys = ["clothing_article_id", "outfit_id"])
data class OutfitArticlesEntity(
    @ColumnInfo(name = "clothing_article_id") val clothingArticleId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "outfit_id") val outfitId: String = UUID.randomUUID().toString(),
)

@Entity(tableName = "clothing_article_images",
        indices = [Index(value = ["clothing_article_id"])])
data class ClothingArticleImageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "clothing_article_id") val clothingArticleId: String,
    @ColumnInfo(name = "uri") val uri: String,
    @ColumnInfo(name = "thumbnail_uri") val thumbnailUri: String,
    // TODO: image rank
)

data class ClothingArticleWithImagesEntity(
    @Embedded val clothingArticleEntity: ClothingArticleEntity,
    @Relation(parentColumn = "id", entityColumn = "clothing_article_id")
    val images: List<ClothingArticleImageEntity>
)

@Dao
interface OutfitArticlesDao {
    @Insert
    fun insertOutfit(vararg outfitEntity: OutfitEntity)
    @Update
    fun updateOutfit(outfitEntity: OutfitEntity)

    @Insert
    fun insertOutfitArticle(vararg outfitArticlesEntity: OutfitArticlesEntity)

    @Query(""" SELECT clothing_articles.*
                FROM clothing_articles
                JOIN outfit_articles ON clothing_articles.id = outfit_articles.clothing_article_id
                WHERE outfit_articles.outfit_id = :outfitId """)
    fun getAllOutfitArticles(outfitId: String): LiveData<List<ClothingArticleEntity>>
}

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
    fun getClothingArticleWithImagesLive(clothingArticleId: String): LiveData<ClothingArticleWithImagesEntity>

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

@Database(entities = [ClothingArticleEntity::class,
                        ClothingArticleImageEntity::class,
                        OutfitEntity::class,
                        OutfitArticlesEntity::class,],
                    version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clothingArticleWithImagesDao(): ClothingArticleWithImagesDao
    abstract fun outfitArticlesDao(): OutfitArticlesDao
}