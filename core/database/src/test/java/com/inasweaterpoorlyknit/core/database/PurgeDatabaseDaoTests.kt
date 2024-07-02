package com.inasweaterpoorlyknit.core.database

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class PurgeDatabaseDaoTests: DatabaseTests() {
  @Test
  fun purgeDatabase() = runBlocking{
    val entityCount = 10
    articleDao.insertArticles(*createArticleEntity(entityCount))
    ensembleDao.insertEnsembles(*createEnsembleEntity(entityCount))
    val allArticlesBefore = articleDao.getCountArticles().first()
    val allEnsemblesBefore = ensembleDao.getCountEnsembles().first()

    purgeDatabaseDao.purgeDatabase()

    val allArticlesAfter = articleDao.getCountArticles().first()
    val allEnsemblesAfter = ensembleDao.getCountEnsembles().first()
    assertEquals(entityCount, allArticlesBefore)
    assertEquals(entityCount, allEnsemblesBefore)
    assertEquals(0, allArticlesAfter)
    assertEquals(0, allEnsemblesAfter)
  }
}