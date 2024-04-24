package com.inasweaterpoorlyknit.inknit.database.model

import android.net.Uri
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
)

@Entity(
    tableName = "clothing_article_images",
    indices = [Index(value = ["clothing_article_id"])]
)
data class ClothingArticleImageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "clothing_article_id") val clothingArticleId: String,
    @ColumnInfo(name = "image_uri") val uri: Uri?,
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
interface ClothingArticleDao {
    @Query("SELECT * FROM clothing_articles")
    fun getAll(): List<ClothingArticleEntity>
    @Insert
    fun insert(vararg clothingArticleEntity: ClothingArticleEntity)
    @Delete
    fun delete(clothingArticleEntity: ClothingArticleEntity)
    @Update
    fun update(clothingArticleEntity: ClothingArticleEntity)
}

@Dao
interface ClothingArticleImageDao {
    @Query("""SELECT * FROM clothing_article_images
              WHERE clothing_article_images.clothing_article_id = :clothingArticleId""")
    fun getImages(clothingArticleId: String): ClothingArticleImageEntity
    @Insert
    fun insert(vararg clothingArticleImageEntity: ClothingArticleImageEntity)
}

@Dao
interface ClothingArticleWithImagesDao {
    @Transaction
    @Query("""SELECT * FROM clothing_articles
              WHERE clothing_articles.id = :clothingArticleId """)
    fun get(clothingArticleId: String): ClothingArticleWithImagesEntity
}

@Database(entities = [ClothingArticleEntity::class, ClothingArticleImageEntity::class], version = 1)
@TypeConverters(UriConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clothingArticleDao(): ClothingArticleDao
    abstract fun clothingArticleImageDao(): ClothingArticleImageDao
    abstract fun clothingArticleWithImagesDao(): ClothingArticleWithImagesDao
}