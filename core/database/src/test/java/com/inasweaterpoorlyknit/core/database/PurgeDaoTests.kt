package com.inasweaterpoorlyknit.core.database

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.dao.PurgeDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class PurgeDaoTests {
  private lateinit var db: NoopDatabase
  private lateinit var articleDao: ArticleDao
  private lateinit var ensembleDao: EnsembleDao
  private lateinit var purgeDao: PurgeDao

  @Before
  fun beforeEach(){
    db = Room.inMemoryDatabaseBuilder(
      InstrumentationRegistry.getInstrumentation().targetContext,
      NoopDatabase::class.java
    ).allowMainThreadQueries()
        .build()
    articleDao = db.ArticleDao()
    ensembleDao = db.EnsembleDao()
    purgeDao = PurgeDao(db)
  }

  @Test
  fun purgeDatabase() = runBlocking{
    val entityCount = 10
    articleDao.insertArticles(*createArticleEntity(entityCount))
    ensembleDao.insertEnsembles(*createEnsembleEntity(entityCount))
    val allArticlesBefore = articleDao.getArticlesCount().first()
    val allEnsemblesBefore = ensembleDao.getEnsemblesCount().first()

    purgeDao.purgeDatabase()

    val allArticlesAfter = articleDao.getArticlesCount().first()
    val allEnsemblesAfter = ensembleDao.getEnsemblesCount().first()
    assertEquals(entityCount, allArticlesBefore)
    assertEquals(entityCount, allEnsemblesBefore)
    assertEquals(0, allArticlesAfter)
    assertEquals(0, allEnsemblesAfter)
  }
}