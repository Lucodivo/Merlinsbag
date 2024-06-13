package com.inasweaterpoorlyknit.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.inasweaterpoorlyknit.core.database.model.ArticleWithFullImages
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleThumbnails
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnsembleDao {
  @Insert fun insertEnsembles(vararg ensembleEntity: EnsembleEntity)
  @Insert fun insertArticleEnsemble(vararg ensembleArticleEntity: EnsembleArticleEntity)
  @Transaction fun insertEnsembleWithArticles(title: String, articleIds: List<String>) {
    val created = generateTime()
    val ensemble = EnsembleEntity(id = generateId(), title = title, created = created, modified = created)
    insertEnsembles(ensemble)
    insertArticleEnsemble(*articleIds.map{ EnsembleArticleEntity(ensemble.id, it) }.toTypedArray())
  }

  @Query("""DELETE FROM ensemble WHERE id = :ensembleId""") fun deleteEnsemble(ensembleId: String)
  @Query("""DELETE FROM ensemble""") fun deleteAllEnsembles()

  @Query("""UPDATE ensemble
            SET title = :title,
                modified = :modified
            WHERE id = :ensembleId """)
  fun updateEnsemble(ensembleId: String, title: String, modified: Long = generateTime())
  fun updateEnsemble(ensemble: Ensemble) = updateEnsemble(ensemble.id, ensemble.title)

  @Query("""DELETE FROM ensemble_article 
            WHERE ensemble_id = :ensembleId AND article_id IN (:articleIds)""")
  fun deleteArticleEnsembles(ensembleId: String, articleIds: List<String>)

  @Transaction @Query(
    """SELECT ensemble_article.article_id as article_id 
      FROM ensemble_article 
      JOIN article ON article.id = article_id
      WHERE ensemble_article.ensemble_id = :ensembleId""")
  fun getEnsembleArticleThumbnails(ensembleId: String): Flow<List<ArticleWithThumbnails>>

  @Transaction @Query(
    """SELECT ensemble_article.article_id as article_id 
      FROM ensemble_article 
      JOIN article ON article.id = article_id
      WHERE ensemble_article.ensemble_id = :ensembleId""")
  fun getEnsembleArticleFullImages(ensembleId: String): Flow<List<ArticleWithFullImages>>

  @Transaction @Query(
    """SELECT ensemble.id as ensemble_id, ensemble.title as ensemble_title 
       FROM ensemble
       ORDER BY modified DESC """)
  fun getAllEnsembleArticleThumbnails(): Flow<List<EnsembleArticleThumbnails>>

  @Query("""SELECT ensemble.id, ensemble.title 
            FROM ensemble
            WHERE id = :ensembleId """)
  fun getEnsemble(ensembleId: String): Flow<Ensemble>

  // NOTE: Only used for tests
  @Query("""SELECT COUNT(id) FROM ensemble""") fun getEnsemblesCount(): Flow<Int>

  @Transaction @Query(
    """SELECT ensemble.id as ensemble_id, ensemble.title as ensemble_title 
       FROM ensemble
       JOIN ensemble_fts ON ensemble.id = ensemble_fts.id
       WHERE ensemble_fts MATCH :query
       ORDER BY modified DESC """)
  fun searchEnsembleArticleThumbnails(query: String): Flow<List<EnsembleArticleThumbnails>>
}
