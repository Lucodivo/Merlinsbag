package com.inasweaterpoorlyknit.core.data.repository

import android.content.Context
import com.inasweaterpoorlyknit.core.common.listMap
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleEntity
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.model.LazyEnsembleThumbnails
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleThumbnails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EnsembleRepository(
  private val context: Context,
  private val ensembleDao: EnsembleDao
) {
  val directory = articleFilesDirStr(context)

  private fun EnsembleArticleThumbnails.toLazyEnsembleThumbnails() =
    LazyEnsembleThumbnails(
      Ensemble(
        ensembleId,
        ensembleTitle
      ),
      LazyArticleThumbnails(directory, articles)
    )

  fun getAllEnsembleArticleThumbnails(): Flow<List<LazyEnsembleThumbnails>> =
      ensembleDao.getAllEnsembleArticleThumbnails().listMap { it.toLazyEnsembleThumbnails() }
  fun searchEnsembleArticleThumbnails(query: String): Flow<List<LazyEnsembleThumbnails>> =
      ensembleDao.searchEnsembleArticleThumbnails(query).listMap { it.toLazyEnsembleThumbnails() }
  fun getEnsembleArticleThumbnails(ensembleId: String) = ensembleDao.getEnsembleArticleThumbnails(ensembleId).map{ LazyArticleThumbnails(articleFilesDirStr(context), it) }
  fun getEnsemble(ensembleId: String): Flow<Ensemble> = ensembleDao.getEnsemble(ensembleId)
  fun insertEnsemble(title: String, articleIds: List<String>) = ensembleDao.insertEnsembleWithArticles(title, articleIds)
  fun deleteEnsembleArticles(ensembleId: String, articleIds: List<String>) = ensembleDao.deleteArticleEnsembles(ensembleId, articleIds)
  fun updateEnsemble(updatedEnsemble: Ensemble) = ensembleDao.updateEnsemble(updatedEnsemble)
  fun addEnsembleArticles(ensembleId: String, articleIds: List<String>) {
    val ensembleArticles = Array(articleIds.size){ EnsembleArticleEntity(ensembleId, articleIds[it]) }
    ensembleDao.insertArticleEnsemble(*ensembleArticles)
  }
  fun deleteEnsemble(ensembleId: String) = ensembleDao.deleteEnsemble(ensembleId)
  fun deleteEnsembles(ensembleIds: List<String>) = ensembleDao.deleteEnsembles(ensembleIds)
}