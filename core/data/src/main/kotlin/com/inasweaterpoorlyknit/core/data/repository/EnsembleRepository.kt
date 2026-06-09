package com.inasweaterpoorlyknit.core.data.repository

import android.content.Context
import com.inasweaterpoorlyknit.core.common.listMap
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.model.LazyEnsembleThumbnails
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.entity.EnsembleArticleEntity
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleCount
import com.inasweaterpoorlyknit.core.image.articleFilesDirStr
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EnsembleRepository(context: Context, private val ensembleDao: EnsembleDao) {
  private val articleImagesDirStr = articleFilesDirStr(context)

  fun addEnsemble(title: String, articleIds: List<String>) = ensembleDao.insertEnsembleWithArticles(title, articleIds)
  fun addArticlesToEnsemble(ensembleId: String, articleIds: List<String>) {
    val ensembleArticles = Array(articleIds.size){ EnsembleArticleEntity(ensembleId = ensembleId, articleId = articleIds[it]) }
    ensembleDao.insertArticleEnsemble(*ensembleArticles)
    ensembleDao.updateEnsemblesModified(ensembleIds = listOf(ensembleId))
  }
  fun addEnsemblesToArticle(articleId: String, ensembleIds: List<String>) {
    val ensembleArticles = Array(ensembleIds.size){ EnsembleArticleEntity(ensembleId = ensembleIds[it], articleId = articleId) }
    ensembleDao.insertArticleEnsemble(*ensembleArticles)
    ensembleDao.updateEnsemblesModified(ensembleIds = ensembleIds)
  }

  fun updateEnsemble(updatedEnsemble: Ensemble) = ensembleDao.updateEnsemble(updatedEnsemble)

  fun getCountEnsembles(): Flow<Int> = ensembleDao.getCountEnsembles()
  fun getAllEnsembles(): Flow<List<Ensemble>> = ensembleDao.getAllEnsembles()
  fun getAllEnsembleArticleThumbnails(): Flow<List<LazyEnsembleThumbnails>> =
      ensembleDao.getAllEnsembleArticleThumbnails().listMap {
        LazyEnsembleThumbnails.fromEnsembleArticleThumbnails(it, articleImagesDirStr)
      }
  fun getEnsembleArticleThumbnails(ensembleId: String): Flow<LazyArticleThumbnails> =
    ensembleDao.getEnsembleArticleThumbnails(ensembleId).map{ LazyArticleThumbnails(articleImagesDirStr, it) }
  fun getEnsemble(ensembleId: String): Flow<Ensemble> = ensembleDao.getEnsemble(ensembleId)
  fun getEnsemblesByArticle(articleId: String): Flow<List<Ensemble>> = ensembleDao.getEnsemblesByArticle(articleId)
  fun getMostPopularEnsembles(count: Int): Flow<List<EnsembleArticleCount>> = ensembleDao.getMostPopularEnsembles(count)
  fun getMostPopularArticlesEnsembleCount(count: Int): Flow<Pair<List<Int>, LazyUriStrings>> =
    ensembleDao.getMostPopularArticlesEnsembleCount(count).map { articleEnsembleCounts ->
      Pair(
        articleEnsembleCounts.map { it.ensembleCount },
        LazyArticleThumbnails(articleImagesDirStr, articleEnsembleCounts)
      )
    }

  fun searchAllEnsembles(query: String): Flow<List<Ensemble>> = ensembleDao.searchEnsemble("$query*")
  fun searchEnsembleArticleThumbnails(query: String): Flow<List<LazyEnsembleThumbnails>> =
    ensembleDao.searchEnsembleArticleThumbnails(query).listMap {
      LazyEnsembleThumbnails.fromEnsembleArticleThumbnails(it, articleImagesDirStr)
    }

  fun isEnsembleTitleUnique(title: String): Flow<Boolean> = ensembleDao.existsEnsembleTitle(title).map { !it }

  fun deleteEnsemble(ensembleId: String) = ensembleDao.deleteEnsemble(ensembleId)
  fun deleteEnsembles(ensembleIds: List<String>) = ensembleDao.deleteEnsembles(ensembleIds)
  fun deleteArticlesFromEnsemble(ensembleId: String, articleIds: List<String>) {
    ensembleDao.deleteArticleEnsemblesFromEnsemble(ensembleId = ensembleId, articleIds = articleIds)
    ensembleDao.updateEnsemblesModified(ensembleIds = listOf(ensembleId))
  }
  fun deleteEnsemblesFromArticle(articleId: String, ensembleIds: List<String>) {
    ensembleDao.deleteArticleEnsemblesFromArticle(articleId = articleId, ensembleIds = ensembleIds)
    ensembleDao.updateEnsemblesModified(ensembleIds = ensembleIds)
  }
}