package com.inasweaterpoorlyknit.core.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
  fun searchEnsembleWithArticleThumbnails() = runBlocking(Dispatchers.IO) {
    val articles = createArticleEntity(15)
    val articleIds = Array(articles.size){ articles[it].id }
    val articleImages = Array(articles.size){ createArticleImageEntity(articleIds[it]) }
    val ensembleTitles = arrayOf(
      "Road Warrior",
      "The Railway",
      "The Rangers",
      "Fantastic Raddish Warlock",
      "Secret Way of Fans"
    ).apply { sort() }
    val ensembleArticles = Array(ensembleTitles.size){
      articleIds.slice((0 + it) until (articles.lastIndex - it))
    }

    articleDao.insertArticles(*articles)
    articleDao.insertArticleImages(*articleImages)
    ensembleTitles.forEachIndexed{i, it ->
      ensembleDao.insertEnsembleWithArticles(it, ensembleArticles[i])
    }

    val ensembleSearch_rStar = ensembleDao.searchEnsembleArticleThumbnails("r*").first().toTypedArray() // Road Warrior, The Railway, Fantastic Raddish Warlock, The Rangers
    val ensembleSearch_thStar = ensembleDao.searchEnsembleArticleThumbnails("th*").first().toTypedArray() // The Railway, The Rangers
    val ensembleSearch_the = ensembleDao.searchEnsembleArticleThumbnails("THE").first().toTypedArray() // The Road, The Shire
    val ensembleSearch_theRaStar = ensembleDao.searchEnsembleArticleThumbnails("the ra*").first().toTypedArray() // The Road, The Shire
    val ensembleSearch_fanStar = ensembleDao.searchEnsembleArticleThumbnails("fan*").first().toTypedArray() // Fantastic Raddish Warlock, Secret Life of Fans
    val ensembleSearch_waStar = ensembleDao.searchEnsembleArticleThumbnails("wa*").first().toTypedArray() // Road Warrior, Fantastic Raddish Warlock, Secret Way of Fans
    val ensembleSearch_fantastic = ensembleDao.searchEnsembleArticleThumbnails("fantastic*").first().toTypedArray() // Fantastic Raddish Warlock
    val ensembleSearch_zStar = ensembleDao.searchEnsembleArticleThumbnails("z*").first().toTypedArray() // none

    assertEquals(4, ensembleSearch_rStar.size)
    assertEquals(2, ensembleSearch_thStar.size)
    assertEquals(2, ensembleSearch_the.size)
    assertEquals(2, ensembleSearch_theRaStar.size)
    assertEquals(2, ensembleSearch_fanStar.size)
    assertEquals(3, ensembleSearch_waStar.size)
    assertEquals(1, ensembleSearch_fantastic.size)
    assertEquals(0, ensembleSearch_zStar.size)

    arrayOf(
      *ensembleSearch_rStar,
      *ensembleSearch_thStar,
      *ensembleSearch_the,
      *ensembleSearch_theRaStar,
      *ensembleSearch_fanStar,
      *ensembleSearch_waStar,
      *ensembleSearch_fantastic,
      *ensembleSearch_zStar,
    ).forEach {
      val index = ensembleTitles.indexOf(it.ensembleTitle)
      assertEquals(ensembleArticles[index].size, it.articles.size)
      assertNotEquals(0, it.articles.size)
    }
  }
}