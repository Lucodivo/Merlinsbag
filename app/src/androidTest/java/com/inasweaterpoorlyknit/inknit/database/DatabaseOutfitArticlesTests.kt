package com.inasweaterpoorlyknit.inknit.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inasweaterpoorlyknit.inknit.database.model.AppDatabase
import com.inasweaterpoorlyknit.inknit.database.model.ClothingArticleWithImagesDao
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.inasweaterpoorlyknit.inknit.database.model.ClothingArticleEntity
import com.inasweaterpoorlyknit.inknit.database.model.ClothingArticleWithImagesEntity
import com.inasweaterpoorlyknit.inknit.database.model.OutfitArticlesDao
import com.inasweaterpoorlyknit.inknit.database.model.OutfitArticlesEntity
import com.inasweaterpoorlyknit.inknit.database.model.OutfitEntity
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals

// These tests are for baseline sanity of the database.
// If these aren't passing, something must be  wrong with the database as a whole.
@RunWith(AndroidJUnit4::class)
class DatabaseOutfitArticlesTests {
  private lateinit var outfitArticlesDao: OutfitArticlesDao
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
    outfitArticlesDao = db.outfitArticlesDao()
    clothingArticleWithImagesDao = db.clothingArticleWithImagesDao()
  }

  @After
  @Throws(IOException::class)
  fun closeDb() {
    db.close()
  }

  @Test
  @Throws(Exception::class)
  fun getAllOutfitArticles() {
    // arrange
    val newOutfit = OutfitEntity()
    val newClothingArticles = createClothingArticleEntity(3)
      .apply{ sortBy{ it.id }  }
    val newOutfitArticles = Array(newClothingArticles.size){ index ->
      OutfitArticlesEntity(clothingArticleId = newClothingArticles[index].id, outfitId = newOutfit.id)
    }

    // act
    outfitArticlesDao.insertOutfit(newOutfit)
    clothingArticleWithImagesDao.insertClothingArticles(*newClothingArticles)
    outfitArticlesDao.insertOutfitArticle(*newOutfitArticles)
    val outfitArticles = LiveDataTestUtil<List<ClothingArticleEntity>>()
      .getValue(outfitArticlesDao.getAllOutfitArticles(newOutfit.id))
      .apply {sortedBy { it.id }}

    // assert
    assertArrayEquals("clothing articles not added to array", newClothingArticles.map{it.id}.toTypedArray(), outfitArticles.map{it.id}.toTypedArray())
  }
}