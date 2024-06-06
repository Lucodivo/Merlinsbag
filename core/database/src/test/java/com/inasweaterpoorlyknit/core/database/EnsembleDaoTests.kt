package com.inasweaterpoorlyknit.core.database

import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleThumbnails
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class EnsembleDaoTests: DatabaseTests() {
  @Test
  fun getEnsembleArticles() {
    // arrange
    val newEnsemble = EnsembleEntity(title = "TestEnsemble")
    val newArticles = createArticleEntity(3)
        .apply{ sortBy{ it.id }  }
    val newEnsembleArticles = Array(newArticles.size){ index ->
      EnsembleArticleEntity(articleId = newArticles[index].id, ensembleId = newEnsemble.id)
    }
    val newArticleImages = Array(newArticles.size){ index ->
      createArticleImageEntity(newArticles[index].id)
    }

    // act
    ensembleDao.insertEnsembles(newEnsemble)
    articleDao.insertArticles(*newArticles)
    articleDao.insertArticleImages(*newArticleImages)
    ensembleDao.insertArticleEnsemble(*newEnsembleArticles)
    val ensembleArticles: List<ArticleWithThumbnails>
    runBlocking(Dispatchers.IO) {
      ensembleArticles = ensembleDao.getEnsembleArticleThumbnails(newEnsemble.id).first().sortedBy { it.articleId }
    }

    // assert
    assertArrayEquals(" articles not added to array", newArticles.map{it.id}.toTypedArray(), ensembleArticles.map{it.articleId}.toTypedArray())
  }

  @Test
  fun getAllEnsembleArticles() {
    // arrange
    val newEnsemble = createEnsembleEntity(5).apply { sortBy { it.id } }
    val newArticles = createArticleEntity(15).apply { sortBy{ it.id } }
    val newEnsembleArticles = Array(newArticles.size){ index ->
      val newEnsembleId = newEnsemble[when(index) {
        in 0..2 -> { 0 }
        in 3..3 -> { 1 }
        in 4..8 -> { 2 }
        in 9..10 -> { 3 }
        else -> { 4 }
      }].id
      EnsembleArticleEntity(articleId = newArticles[index].id, ensembleId = newEnsembleId)
    }.apply { sortBy{ it.ensembleId } }
    val newArticleImages = Array(newArticles.size){ index ->
      createArticleImageEntity(newArticles[index].id)
    }

    // act
    ensembleDao.insertEnsembles(*newEnsemble)
    articleDao.insertArticles(*newArticles)
    articleDao.insertArticleImages(*newArticleImages)
    ensembleDao.insertArticleEnsemble(*newEnsembleArticles)
    val actualEnsembleArticles: List<EnsembleArticleThumbnails>
    runBlocking(Dispatchers.IO) {
      actualEnsembleArticles = ensembleDao.getAllEnsembleArticleImages().first()
    }

    // assert
    val expectedEnsembleArticles = newEnsembleArticles.groupBy { it.ensembleId }.values.toTypedArray().apply { sortBy { it.first().ensembleId } }
    assertEquals(expectedEnsembleArticles.size, actualEnsembleArticles.size)
    assertEquals(expectedEnsembleArticles[0].size, actualEnsembleArticles[0].articles.size)
    assertEquals(expectedEnsembleArticles[1].size, actualEnsembleArticles[1].articles.size)
    assertEquals(expectedEnsembleArticles[2].size, actualEnsembleArticles[2].articles.size)
    assertEquals(expectedEnsembleArticles[3].size, actualEnsembleArticles[3].articles.size)
    assertEquals(expectedEnsembleArticles[4].size, actualEnsembleArticles[4].articles.size)
  }
}