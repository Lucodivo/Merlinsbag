package com.inasweaterpoorlyknit.inknit.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inasweaterpoorlyknit.inknit.database.model.AppDatabase
import com.inasweaterpoorlyknit.inknit.database.model.ClothingArticleDao
import com.inasweaterpoorlyknit.inknit.database.model.ClothingArticleEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

// These tests are for baseline sanity of the database.
// If these aren't passing, something must be  wrong with the database as a whole.
@RunWith(AndroidJUnit4::class)
class DatabaseSanityTests {
    private lateinit var clothingArticleDao: ClothingArticleDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        clothingArticleDao = db.clothingArticleDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() {
        val clothingArticlesToInsert = createClothingArticleEntity(3)
        clothingArticleDao.insert(*clothingArticlesToInsert)
        val clothingArticlesFromDatabase = clothingArticleDao.getAll()
        assertEquals("Could not insert and retrieve ClothingArticleEntities!", clothingArticlesToInsert.size, clothingArticlesFromDatabase.size)
    }
}
