package com.inasweaterpoorlyknit.inknit.database.model

import android.net.Uri
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
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import java.util.UUID

// TODO: Just use URI instead of String
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
    @ColumnInfo(name = "uri") val uri: Uri?,
    @ColumnInfo(name = "thumbnail_uri") val thumbnailUri: Uri?,
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

class UriConverter {
    @TypeConverter
    fun fromString(value: String?): Uri? = value?.let{ Uri.parse(value) }
    @TypeConverter
    fun toString(uri: Uri?): String? = uri?.toString()
}

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

    // TODO: Remove
    @Transaction
    @Query("""SELECT * FROM clothing_articles""")
    fun getAllClothingArticlesWithImages(): List<ClothingArticleWithImagesEntity>

    @Transaction
    @Query("""SELECT * FROM clothing_articles""")
    fun getAllClothingArticlesWithImagesLive(): LiveData<List<ClothingArticleWithImagesEntity>>

    @Transaction
    fun insertClothingArticle(imageUri: Uri, thumbnailUri: Uri) {
        val clothingArticle = ClothingArticleEntity()
        val clothingArticleImage = ClothingArticleImageEntity(clothingArticleId = clothingArticle.id, uri = imageUri, thumbnailUri = thumbnailUri)
        insertClothingArticles(clothingArticle)
        insertClothingArticleImages(clothingArticleImage)
    }
}

@Database(entities = [ClothingArticleEntity::class, ClothingArticleImageEntity::class], version = 1)
@TypeConverters(UriConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clothingArticleWithImagesDao(): ClothingArticleWithImagesDao
}