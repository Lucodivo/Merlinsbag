package com.inasweaterpoorlyknit.core.database

import com.inasweaterpoorlyknit.core.database.model.EnsembleFtsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
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

  @Test
  fun searchEnsembleWithArticles() = runBlocking(Dispatchers.IO) {
    val ensembleTitles = arrayOf(
      "Road Warrior",
      "The Railway",
      "The Shire",
      "Fantastic Raddish Warlock",
      "Secret Way of Fans"
    ).apply { sort() }
    val ensembleFtsEntities = ensembleTitles.map {
      EnsembleFtsEntity(
        ensembleId = createCounterString(),
        title = it,
      )
    }.toTypedArray()
    ensembleDao.insertEnsemblesFts(*ensembleFtsEntities)

    val ensembleSearch_rStar = ensembleDao.searchEnsembleFtsSingle("r*") // Road Warrior, The Railway, Fantastic Raddish Warlock
    val ensembleSearch_thStar = ensembleDao.searchEnsembleFtsSingle("th*") // The Railway, The Shire
    val ensembleSearch_the = ensembleDao.searchEnsembleFtsSingle("THE") // The Road, The Shire
    val ensembleSearch_fanStar = ensembleDao.searchEnsembleFtsSingle("fan*") // Fantastic Raddish Warlock, Secret Life of Fans
    val ensembleSearch_waStar = ensembleDao.searchEnsembleFtsSingle("wa*") // Road Warrior, Fantastic Raddish Warlock, Secret Way of Fans
    val ensembleSearch_fantastic = ensembleDao.searchEnsembleFtsSingle("fantastic*") // Fantastic Raddish Warlock
    val ensembleSearch_zStar = ensembleDao.searchEnsembleFtsSingle("z*") // none

    assertEquals(3, ensembleSearch_rStar.size)
    assertEquals(2, ensembleSearch_thStar.size)
    assertEquals(2, ensembleSearch_the.size)
    assertEquals(2, ensembleSearch_fanStar.size)
    assertEquals(3, ensembleSearch_waStar.size)
    assertEquals(1, ensembleSearch_fantastic.size)
    assertEquals(0, ensembleSearch_zStar.size)
  }
}