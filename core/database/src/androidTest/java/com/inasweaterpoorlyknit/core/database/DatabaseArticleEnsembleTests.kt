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
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.model.ArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ArticleEnsembleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import org.junit.Assert.assertArrayEquals

// These tests are for baseline sanity of the database.
// If these aren't passing, something must be  wrong with the database as a whole.
@RunWith(AndroidJUnit4::class)
class DatabaseArticleEnsembleTests {
  private lateinit var ensembleDao: EnsembleDao
  private lateinit var articleDao: ArticleDao
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
    ensembleDao = db.EnsembleDao()
    articleDao = db.ArticleDao()
  }

  @After
  @Throws(IOException::class)
  fun closeDb() {
    db.close()
  }

  @Test
  @Throws(Exception::class)
  fun getAllEnsembleArticles() {
    // arrange
    val newEnsemble = EnsembleEntity(title = "TestEnsemble")
    val newArticles = createArticleEntity(3)
      .apply{ sortBy{ it.id }  }
    val newEnsembleArticles = Array(newArticles.size){ index ->
      ArticleEnsembleEntity(articleId = newArticles[index].id, ensembleId = newEnsemble.id)
    }

    // act
    ensembleDao.insertEnsemble(newEnsemble)
    articleDao.insertArticles(*newArticles)
    ensembleDao.insertArticleEnsemble(*newEnsembleArticles)
    val ensembleArticles = LiveDataTestUtil<List<ArticleEntity>>()
      .getValue(ensembleDao.getAllEnsembleArticles(newEnsemble.id))
      .apply {sortedBy { it.id }}

    // assert
    assertArrayEquals(" articles not added to array", newArticles.map{it.id}.toTypedArray(), ensembleArticles.map{it.id}.toTypedArray())
  }
}