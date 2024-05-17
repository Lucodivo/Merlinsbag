package com.inasweaterpoorlyknit.core.database.repository

import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.dao.EnsembleArticles
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import kotlinx.coroutines.flow.Flow

class EnsembleRepository(
  private val ensembleDao: EnsembleDao
) {
  fun getAllEnsembleArticleImages(): Flow<List<EnsembleArticles>> = ensembleDao.getAllEnsembleArticleImages()
  fun getEnsembleArticleImages(ensembleId: String): Flow<List<ArticleWithImages>> = ensembleDao.getEnsembleArticleImages(ensembleId)
  fun getEnsemble(ensembleId: String): Flow<EnsembleEntity> = ensembleDao.getEnsemble(ensembleId)
  fun insertEnsemble(title: String, articleIds: List<String>) = ensembleDao.insertEnsembleWithArticles(
    ensemble = EnsembleEntity(title = title),
    articleIds = articleIds
  )

  fun updateEnsemble(updatedEnsemble: EnsembleEntity) {
    ensembleDao.updateEnsemble(updatedEnsemble)
  }
}