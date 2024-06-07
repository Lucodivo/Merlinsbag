package com.inasweaterpoorlyknit.core.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class EnsembleDaoTests: DatabaseTests() {
  @Test
  fun insertEnsembleWithArticles() = runBlocking(Dispatchers.IO) {
    val articles = createArticleEntity(15)
    val articleIds = Array(articles.size){ articles[it].id }
    val newArticleImages = Array(articles.size){ createArticleImageEntity(articleIds[it]) }
    val ensembleTitles = createCounterString(5)
    val ensembleArticles = Array(5){ articleIds.slice((0 + it) until (articles.lastIndex - it)) }

    articleDao.insertArticles(*articles)
    articleDao.insertArticleImages(*newArticleImages)
    ensembleTitles.forEachIndexed{i, it ->
      ensembleDao.insertEnsembleWithArticles(it, ensembleArticles[i])
    }

    val actualEnsembleArticles = ensembleDao.getAllEnsembleArticleThumbnails().first().sortedBy { it.ensembleTitle.toInt() }
    assertEquals(ensembleTitles.size, actualEnsembleArticles.size)
    assertEquals(ensembleArticles[0].size, actualEnsembleArticles[0].articles.size)
    assertEquals(ensembleArticles[1].size, actualEnsembleArticles[1].articles.size)
    assertEquals(ensembleArticles[2].size, actualEnsembleArticles[2].articles.size)
    assertEquals(ensembleArticles[3].size, actualEnsembleArticles[3].articles.size)
    assertEquals(ensembleArticles[4].size, actualEnsembleArticles[4].articles.size)
  }
}