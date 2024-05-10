package com.inasweaterpoorlyknit.core.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.inasweaterpoorlyknit.core.database.model.ArticleImage
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnsembleDao {
  @Insert
  fun insertEnsemble(vararg ensembleEntity: EnsembleEntity)
  @Update
  fun updateEnsemble(ensembleEntity: EnsembleEntity)
  @Insert
  fun insertArticleEnsemble(vararg ensembleArticleEntity: EnsembleArticleEntity)

  @Transaction
  @Query(
    """SELECT article_image.article_id, article_image.uri, article_image.thumb_uri
       FROM article_image
       JOIN article ON article.id = article_image.article_id
       JOIN ensemble_article ON ensemble_article.article_id = article.id
       WHERE ensemble_article.ensemble_id = :ensembleId"""
  )
  fun getAllEnsembleArticleImages(ensembleId: String): Flow<List<ArticleImage>>

  @Query("SELECT * FROM ensemble")
  fun getAllEnsembles(): Flow<List<EnsembleEntity>>

  @Query(
    """ SELECT * FROM ensemble
        WHERE id = :ensembleId """
  )
  fun getEnsemble(ensembleId: String): Flow<EnsembleEntity>
}
