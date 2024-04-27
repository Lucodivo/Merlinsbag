package com.inasweaterpoorlyknit.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inasweaterpoorlyknit.core.database.model.AppDatabase
import com.inasweaterpoorlyknit.core.database.model.ClothingArticleWithImagesDao
import com.inasweaterpoorlyknit.core.database.model.ClothingArticleWithImagesEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

// These tests are for baseline sanity of the database.
// If these aren't passing, something must be  wrong with the database as a whole.
@RunWith(AndroidJUnit4::class)
class DatabaseClothingArticlesWithImagesTests {
    private lateinit var clothingArticleWithImagesDao: ClothingArticleWithImagesDao
    private lateinit var db: AppDatabase

    // NOTE: Used to observeForever on the main thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        clothingArticleWithImagesDao = db.clothingArticleWithImagesDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() { db.close() }

    @Test
    @Throws(Exception::class)
    fun insertClothingArticle() {
        // arrange
        val fullImageUris = createFakeUriStrings(2)
        val thumbnailUriImageUris = createFakeUriStrings(2)

        // act
        clothingArticleWithImagesDao.insertClothingArticle(fullImageUris[0], thumbnailUriImageUris[0])
        clothingArticleWithImagesDao.insertClothingArticle(fullImageUris[1], thumbnailUriImageUris[1])
        val clothingArticlesWithImages = LiveDataTestUtil<List<ClothingArticleWithImagesEntity>>()
            .getValue(clothingArticleWithImagesDao.getAllClothingArticlesWithImages())

        // assert
        assertEquals("clothing articles not properly inserted and retreived", fullImageUris.size, clothingArticlesWithImages.size)
    }

    @Test
    @Throws(Exception::class)
    fun clothingInsertArticleWithImages() {
        // arrange
        val clothingArticlesToInsert = createClothingArticleEntity(  3)
        val clothingArticleImagesToInsert1 = createClothingArticleImageEntity(clothingArticleId = clothingArticlesToInsert[0].id)
        val clothingArticleImagesToInsert2 = createClothingArticleImageEntity(2, clothingArticleId = clothingArticlesToInsert[1].id)
        val clothingArticleImagesToInsert3 = createClothingArticleImageEntity(3, clothingArticleId = clothingArticlesToInsert[2].id)

        // act
        clothingArticleWithImagesDao.insertClothingArticles(*clothingArticlesToInsert)
        clothingArticleWithImagesDao.insertClothingArticleImages(clothingArticleImagesToInsert1)
        clothingArticleWithImagesDao.insertClothingArticleImages(*clothingArticleImagesToInsert2)
        clothingArticleWithImagesDao.insertClothingArticleImages(*clothingArticleImagesToInsert3)
        val clothingArticlesWithImages1 = LiveDataTestUtil<ClothingArticleWithImagesEntity>()
            .getValue(clothingArticleWithImagesDao.getClothingArticleWithImagesLive(clothingArticlesToInsert[0].id))
        val clothingArticlesWithImages2 = LiveDataTestUtil<ClothingArticleWithImagesEntity>()
            .getValue(clothingArticleWithImagesDao.getClothingArticleWithImagesLive(clothingArticlesToInsert[1].id))
        val clothingArticlesWithImages3 = LiveDataTestUtil<ClothingArticleWithImagesEntity>()
            .getValue(clothingArticleWithImagesDao.getClothingArticleWithImagesLive(clothingArticlesToInsert[2].id))

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
