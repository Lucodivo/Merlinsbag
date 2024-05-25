package com.inasweaterpoorlyknit.core.repository

import android.content.Context
import com.inasweaterpoorlyknit.core.common.listMap
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.dao.EnsembleArticles
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import kotlinx.coroutines.flow.Flow

class EnsembleRepository(
  private val context: Context,
  private val ensembleDao: EnsembleDao
) {
  fun getAllEnsembleArticleImages(): Flow<List<EnsembleArticles>> = ensembleDao.getAllEnsembleArticleImages().listMap { ensembleArticles ->
    ensembleArticles.copy(
      articles = ensembleArticles.articles.map { it.appendDirectory(context) }
    )
  }
  fun getEnsembleArticleImages(ensembleId: String): Flow<List<ArticleWithImages>> {
    return ensembleDao.getEnsembleArticleImages(ensembleId).listMap { it.appendDirectory(context) }
  }
  fun getEnsemble(ensembleId: String): Flow<EnsembleEntity> = ensembleDao.getEnsemble(ensembleId)
  fun insertEnsemble(title: String, articleIds: List<String>) = ensembleDao.insertEnsembleWithArticles(EnsembleEntity(title = title), articleIds)
  fun deleteEnsembleArticles(ensembleId: String, articleIds: List<String>) = ensembleDao.deleteArticleEnsembles(ensembleId, articleIds)
  fun updateEnsemble(updatedEnsemble: EnsembleEntity) = ensembleDao.updateEnsemble(updatedEnsemble)
  fun addEnsembleArticles(ensembleId: String, articleIds: List<String>) {
    val ensembleArticles = Array(articleIds.size){ index ->
      EnsembleArticleEntity(ensembleId = ensembleId, articleId = articleIds[index])
    }
    ensembleDao.insertArticleEnsemble(*ensembleArticles)
  }
}