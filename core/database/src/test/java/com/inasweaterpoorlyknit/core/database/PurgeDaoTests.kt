package com.inasweaterpoorlyknit.core.database

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class PurgeDaoTests: DatabaseTests() {
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