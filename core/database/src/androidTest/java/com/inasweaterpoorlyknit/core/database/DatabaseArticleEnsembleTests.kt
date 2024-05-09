package com.inasweaterpoorlyknit.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImagesDao
import com.inasweaterpoorlyknit.core.database.dao.ArticleEnsembleDao
import com.inasweaterpoorlyknit.core.database.model.ArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ArticleEnsembleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import org.junit.Assert.assertArrayEquals

// These tests are for baseline sanity of the database.
// If these aren't passing, something must be  wrong with the database as a whole.
@RunWith(AndroidJUnit4::class)
class DatabaseArticleEnsembleTests {
  private lateinit var articleEnsembleDao: ArticleEnsembleDao
  private lateinit var articleWithImagesDao: ArticleWithImagesDao
  private lateinit var db: InKnitDatabase

  // NOTE: Used to observeForever on the main thread
  @get:Rule
  var instantTaskExecutorRule = InstantTaskExecutorRule()

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, InKnitDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    articleEnsembleDao = db.ArticleEnsembleDao()
    articleWithImagesDao = db.ArticleWithImagesDao()
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
    val newOutfit = EnsembleEntity()
    val newArticles = createArticleEntity(3)
      .apply{ sortBy{ it.id }  }
    val newOutfitArticles = Array(newArticles.size){ index ->
      ArticleEnsembleEntity(articleId = newArticles[index].id, ensembleId = newOutfit.id)
    }

    // act
    articleEnsembleDao.insertEnsemble(newOutfit)
    articleWithImagesDao.insertArticles(*newArticles)
    articleEnsembleDao.insertArticleEnsemble(*newOutfitArticles)
    val outfitArticles = LiveDataTestUtil<List<ArticleEntity>>()
      .getValue(articleEnsembleDao.getAllEnsembleArticles(newOutfit.id))
      .apply {sortedBy { it.id }}

    // assert
    assertArrayEquals(" articles not added to array", newArticles.map{it.id}.toTypedArray(), outfitArticles.map{it.id}.toTypedArray())
  }
}