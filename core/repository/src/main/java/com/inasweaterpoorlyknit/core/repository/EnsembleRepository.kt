package com.inasweaterpoorlyknit.core.repository

import android.content.Context
import com.inasweaterpoorlyknit.core.common.articleFilesDirStr
import com.inasweaterpoorlyknit.core.common.listMap
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Ensemble(
  val id: String,
  val title: String,
  val thumbnails: LazyArticleThumbnails,
)

class EnsembleRepository(
  private val context: Context,
  private val ensembleDao: EnsembleDao
) {
  fun getAllEnsembleArticleImages(): Flow<List<Ensemble>> {
    return ensembleDao.getAllEnsembleArticleImages().listMap { ensembleArticleImages ->
      Ensemble(
        ensembleArticleImages.ensemble.id,
        ensembleArticleImages.ensemble.title,
        LazyArticleThumbnails(articleFilesDirStr(context), ensembleArticleImages.articles)
      )
    }
  }
  fun getEnsembleArticleImages(ensembleId: String) = ensembleDao.getEnsembleArticleThumbnails(ensembleId).map{ LazyArticleThumbnails(articleFilesDirStr(context), it) }
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