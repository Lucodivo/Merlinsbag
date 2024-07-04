package com.inasweaterpoorlyknit.core.database

import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
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
  fun searchEnsemble() = runBlocking(Dispatchers.IO) {
    val ensembleTitles = arrayOf(
      "Road Warrior",
      "The Railway",
      "The Rangers",
      "Fantastic Raddish Warlock",
      "Secret Way of Fans"
    ).apply { sort() }

    ensembleDao.insertEnsembles(
      *ensembleTitles.map { EnsembleEntity(id = createCounterString(), title = it, created = 0, modified = 0) }.toTypedArray()
    )

    val ensembleSearch_rStar = ensembleDao.searchEnsemble("r*").first().toTypedArray() // Road Warrior, The Railway, Fantastic Raddish Warlock, The Rangers
    val ensembleSearch_thStar = ensembleDao.searchEnsemble("th*").first().toTypedArray() // The Railway, The Rangers
    val ensembleSearch_the = ensembleDao.searchEnsemble("THE").first().toTypedArray() // The Road, The Shire
    val ensembleSearch_theRaStar = ensembleDao.searchEnsemble("the ra*").first().toTypedArray() // The Road, The Shire
    val ensembleSearch_fanStar = ensembleDao.searchEnsemble("fan*").first().toTypedArray() // Fantastic Raddish Warlock, Secret Life of Fans
    val ensembleSearch_waStar = ensembleDao.searchEnsemble("wa*").first().toTypedArray() // Road Warrior, Fantastic Raddish Warlock, Secret Way of Fans
    val ensembleSearch_fantastic = ensembleDao.searchEnsemble("fantastic*").first().toTypedArray() // Fantastic Raddish Warlock
    val ensembleSearch_zStar = ensembleDao.searchEnsemble("z*").first().toTypedArray() // none
    val ensembleSearch_star = ensembleDao.searchEnsemble("*").first().toTypedArray() // none

    assertEquals(4, ensembleSearch_rStar.size)
    assertEquals(2, ensembleSearch_thStar.size)
    assertEquals(2, ensembleSearch_the.size)
    assertEquals(2, ensembleSearch_theRaStar.size)
    assertEquals(2, ensembleSearch_fanStar.size)
    assertEquals(3, ensembleSearch_waStar.size)
    assertEquals(1, ensembleSearch_fantastic.size)
    assertEquals(0, ensembleSearch_zStar.size)
    assertEquals(0, ensembleSearch_star.size)
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

  @Test
  fun getEnsemblesByArticle() = runBlocking(Dispatchers.IO) {
    val ensembles1 = createEnsembleEntity(1).sortedBy { it.title }.toTypedArray()
    val ensembles2 = createEnsembleEntity(5).sortedBy { it.title }.toTypedArray()
    val ensembles3 = createEnsembleEntity(10).sortedBy { it.title }.toTypedArray()
    val ensembles4 = createEnsembleEntity(0).sortedBy { it.title }.toTypedArray()
    val articles = createArticleEntity(4)
    val ensembleArticles1 = Array(ensembles1.size) { EnsembleArticleEntity(ensembleId = ensembles1[it].id, articleId = articles[0].id) }
    val ensembleArticles2 = Array(ensembles2.size) { EnsembleArticleEntity(ensembleId = ensembles2[it].id, articleId = articles[1].id) }
    val ensembleArticles3 = Array(ensembles3.size) { EnsembleArticleEntity(ensembleId = ensembles3[it].id, articleId = articles[2].id) }
    val ensembleArticles4 = Array(ensembles4.size) { EnsembleArticleEntity(ensembleId = ensembles4[it].id, articleId = articles[3].id) }
    articleDao.insertArticles(*articles)
    ensembleDao.insertEnsembles(*ensembles1, *ensembles2, *ensembles3, *ensembles4)
    ensembleDao.insertArticleEnsemble(*ensembleArticles1, *ensembleArticles2, *ensembleArticles3, *ensembleArticles4)

    val ensemblesByArticle1 = ensembleDao.getEnsemblesByArticle(articles[0].id).first().sortedBy { it.title }
    val ensemblesByArticle2 = ensembleDao.getEnsemblesByArticle(articles[1].id).first().sortedBy { it.title }
    val ensemblesByArticle3 = ensembleDao.getEnsemblesByArticle(articles[2].id).first().sortedBy { it.title }
    val ensemblesByArticle4 = ensembleDao.getEnsemblesByArticle(articles[3].id).first().sortedBy { it.title }

    assertEquals(ensembleArticles1.size, ensemblesByArticle1.size)
    assertEquals(ensembleArticles2.size, ensemblesByArticle2.size)
    assertEquals(ensembleArticles3.size, ensemblesByArticle3.size)
    assertEquals(ensembleArticles4.size, ensemblesByArticle4.size)
    ensembles1.forEachIndexed{ i, it -> assertEquals(it.title, ensemblesByArticle1[i].title) }
    ensembles2.forEachIndexed{ i, it -> assertEquals(it.title, ensemblesByArticle2[i].title) }
    ensembles3.forEachIndexed{ i, it -> assertEquals(it.title, ensemblesByArticle3[i].title) }
    ensembles4.forEachIndexed{ i, it -> assertEquals(it.title, ensemblesByArticle4[i].title) }
  }

  @Test
  fun getMostPopularEnsemble() = runBlocking(Dispatchers.IO) {
    val ensembles = Array(3){ createEnsembleEntity(title = "interesting_title$it") }.sortedBy { it.title }.toTypedArray()
    val popularCount = 1
    ensembles.forEachIndexed { index, ensemble ->
      val articles = createArticleEntity(index + 1)
      articleDao.insertArticles(*articles)
      ensembleDao.insertEnsembles(ensemble)
      ensembleDao.insertArticleEnsemble(*articles.map { EnsembleArticleEntity(ensembleId = ensemble.id, articleId = it.id) }.toTypedArray())
    }

    val mostPopularEnsemble = ensembleDao.getMostPopularEnsembles(popularCount).first()

    assertEquals(popularCount, mostPopularEnsemble.size)
    assertEquals(ensembles.last().title, mostPopularEnsemble.first().title)
    assertEquals(ensembles.size.toLong(), mostPopularEnsemble.first().count)
  }

  @Test
  fun getMostPopularArticle() = runBlocking(Dispatchers.IO) {
    val articles = createArticleEntity(3).sortedBy { it.id }.toTypedArray()
    val popularCount = 1
    articles.forEachIndexed { index, article ->
      val ensembles = createEnsembleEntity(index + 1)
      articleDao.insertArticles(article)
      ensembleDao.insertEnsembles(*ensembles)
      ensembleDao.insertArticleEnsemble(*ensembles.map { EnsembleArticleEntity(ensembleId = it.id, articleId = article.id) }.toTypedArray())
    }

    val mostPopularArticle = ensembleDao.getMostPopularArticles(popularCount).first()

    assertEquals(popularCount, mostPopularArticle.size)
    assertEquals(articles.last().id, mostPopularArticle.first().id)
    assertEquals(articles.size.toLong(), mostPopularArticle.first().count)
  }

  @Test
  fun insertEnsembleArticle() = runBlocking(Dispatchers.IO) {
    val article = createArticleEntity()
    val ensemble = createEnsembleEntity()
    val articleEnsemble = EnsembleArticleEntity(ensembleId = ensemble.id, articleId = article.id)
    articleDao.insertArticles(article)
    ensembleDao.insertEnsembles(ensemble)

    ensembleDao.insertArticleEnsemble(articleEnsemble)
    ensembleDao.insertArticleEnsemble(articleEnsemble)

    val ensembleArticleEntities = ensembleDao.getEnsemblesByArticle(article.id).first()
    assertEquals(1, ensembleArticleEntities.size)
    assertEquals(ensemble.id, ensembleArticleEntities.first().id)
    assertEquals(ensemble.title, ensembleArticleEntities.first().title)
  }
}