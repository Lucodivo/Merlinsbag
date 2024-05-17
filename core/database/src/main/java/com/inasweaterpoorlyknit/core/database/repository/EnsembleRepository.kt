package com.inasweaterpoorlyknit.core.database.repository

import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.dao.EnsembleArticles
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.model.ArticleThumbnail
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import com.inasweaterpoorlyknit.core.database.model.toExternalModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EnsembleRepository(
  private val ensembleDao: EnsembleDao
) {
  fun getAllEnsembles(): Flow<List<Ensemble>> = ensembleDao.getAllEnsembles().map { list -> list.map { it.toExternalModel() } }
  fun getAllEnsembleArticleImages(): Flow<List<EnsembleArticles>> = ensembleDao.getAllEnsembleArticleImages()
  fun getEnsembleArticleThumbnails(ensembleId: String): Flow<List<ArticleWithImages>> = ensembleDao.getEnsembleArticleThumbnails(ensembleId)
  fun getEnsemble(ensembleId: String): Flow<Ensemble> = ensembleDao.getEnsemble(ensembleId).map { it.toExternalModel() }
  fun insertEnsemble(
    title: String,
    articleIds: List<String>,
  ) = ensembleDao.insertEnsembleWithArticles(
    ensemble = EnsembleEntity(title = title),
    articleIds = articleIds
  )
}