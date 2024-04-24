package com.inasweaterpoorlyknit.inknit.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inasweaterpoorlyknit.inknit.database.model.AppDatabase
import com.inasweaterpoorlyknit.inknit.database.model.ClothingArticleDao
import com.inasweaterpoorlyknit.inknit.database.model.ClothingArticleImageDao
import com.inasweaterpoorlyknit.inknit.database.model.ClothingArticleWithImagesDao
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

// These tests are for baseline sanity of the database.
// If these aren't passing, something must be  wrong with the database as a whole.
@RunWith(AndroidJUnit4::class)
class DatabaseClothingArticlesWithImagesTests {
    private lateinit var clothingArticleDao: ClothingArticleDao
    private lateinit var clothingArticleImageDao: ClothingArticleImageDao
    private lateinit var clothingArticleWithImagesDao: ClothingArticleWithImagesDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        clothingArticleDao = db.clothingArticleDao()
        clothingArticleImageDao = db.clothingArticleImageDao()
        clothingArticleWithImagesDao = db.clothingArticleWithImagesDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() { db.close() }

    @Test
    @Throws(Exception::class)
    fun clothingArticleWithImages() {
        // arrange
        val clothingArticlesToInsert = createClothingArticleEntity(3)
        val clothingArticleImagesToInsert1 = createClothingArticleImageEntity(clothingArticleId = clothingArticlesToInsert[0].id)
        val clothingArticleImagesToInsert2 = createClothingArticleImageEntity(2, clothingArticleId = clothingArticlesToInsert[1].id)
        val clothingArticleImagesToInsert3 = createClothingArticleImageEntity(3, clothingArticleId = clothingArticlesToInsert[2].id)

        // act
        clothingArticleDao.insert(*clothingArticlesToInsert)
        clothingArticleImageDao.insert(clothingArticleImagesToInsert1)
        clothingArticleImageDao.insert(*clothingArticleImagesToInsert2)
        clothingArticleImageDao.insert(*clothingArticleImagesToInsert3)
        val clothingArticlesWithImages1 = clothingArticleWithImagesDao.get(clothingArticlesToInsert[0].id)
        val clothingArticlesWithImages2 = clothingArticleWithImagesDao.get(clothingArticlesToInsert[1].id)
        val clothingArticlesWithImages3 = clothingArticleWithImagesDao.get(clothingArticlesToInsert[2].id)

        // assert
        assertEquals("Could not acquire clothing article with images", 1, clothingArticlesWithImages1.images.size)
        assertEquals("Clothing article with images contains wrong clothing article id", clothingArticleImagesToInsert1.clothingArticleId, clothingArticlesWithImages1.clothingArticleEntity.id)
        assertEquals("Clothing article with images contains wrong uri", clothingArticleImagesToInsert1.uri, clothingArticlesWithImages1.images[0].uri)
        assertEquals("Could not acquire clothing article with images", clothingArticleImagesToInsert2.size, clothingArticlesWithImages2.images.size)
        assertEquals("Clothing article with images contains wrong clothing article id", clothingArticleImagesToInsert2[0].clothingArticleId, clothingArticlesWithImages2.clothingArticleEntity.id)
        assertEquals("Clothing article with images contains wrong clothing article id", clothingArticleImagesToInsert2[1].clothingArticleId, clothingArticlesWithImages2.clothingArticleEntity.id)
        assertEquals("Clothing article with images contains wrong uri", clothingArticleImagesToInsert2[0].uri, clothingArticlesWithImages2.images[0].uri)
        assertEquals("Clothing article with images contains wrong uri", clothingArticleImagesToInsert2[1].uri, clothingArticlesWithImages2.images[1].uri)
        assertEquals("Could not acquire clothing article with images", clothingArticleImagesToInsert3.size, clothingArticlesWithImages3.images.size)
        assertEquals("Clothing article with images contains wrong clothing article id", clothingArticleImagesToInsert3[0].clothingArticleId, clothingArticlesWithImages3.clothingArticleEntity.id)
        assertEquals("Clothing article with images contains wrong clothing article id", clothingArticleImagesToInsert3[1].clothingArticleId, clothingArticlesWithImages3.clothingArticleEntity.id)
        assertEquals("Clothing article with images contains wrong clothing article id", clothingArticleImagesToInsert3[2].clothingArticleId, clothingArticlesWithImages3.clothingArticleEntity.id)
        assertEquals("Clothing article with images contains wrong uri", clothingArticleImagesToInsert3[0].uri, clothingArticlesWithImages3.images[0].uri)
        assertEquals("Clothing article with images contains wrong uri", clothingArticleImagesToInsert3[1].uri, clothingArticlesWithImages3.images[1].uri)
        assertEquals("Clothing article with images contains wrong uri", clothingArticleImagesToInsert3[2].uri, clothingArticlesWithImages3.images[2].uri)
    }
}
