package com.inasweaterpoorlyknit.core.repository

import android.content.Context
import com.inasweaterpoorlyknit.core.common.articleFilesDirStr
import com.inasweaterpoorlyknit.core.common.listMap
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleEntity
import com.inasweaterpoorlyknit.core.repository.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.repository.model.LazyEnsembleThumbnails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EnsembleRepository(
  private val context: Context,
  private val ensembleDao: EnsembleDao
) {
  fun getAllEnsembleArticleThumbnails(): Flow<List<LazyEnsembleThumbnails>> {
    val directory = articleFilesDirStr(context)
    return ensembleDao.getAllEnsembleArticleImages().listMap { ensembleArticleImages ->
      LazyEnsembleThumbnails(
        ensembleArticleImages.ensembleId,
        ensembleArticleImages.ensembleTitle,
        LazyArticleThumbnails(directory, ensembleArticleImages.articles)
      )
    }
  }
  fun getEnsembleArticleThumbnails(ensembleId: String) = ensembleDao.getEnsembleArticleThumbnails(ensembleId).map{ LazyArticleThumbnails(articleFilesDirStr(context), it) }
  fun getEnsemble(ensembleId: String): Flow<Ensemble> = ensembleDao.getEnsemble(ensembleId)
  fun insertEnsemble(title: String, articleIds: List<String>) = ensembleDao.insertEnsembleWithArticles(title, articleIds)
  fun deleteEnsembleArticles(ensembleId: String, articleIds: List<String>) = ensembleDao.deleteArticleEnsembles(ensembleId, articleIds)
  fun updateEnsemble(updatedEnsemble: Ensemble) = ensembleDao.updateEnsemble(updatedEnsemble)
  fun addEnsembleArticles(ensembleId: String, articleIds: List<String>) {
    val ensembleArticles = Array(articleIds.size){ EnsembleArticleEntity(ensembleId, articleIds[it]) }
    ensembleDao.insertArticleEnsemble(*ensembleArticles)
  }
}