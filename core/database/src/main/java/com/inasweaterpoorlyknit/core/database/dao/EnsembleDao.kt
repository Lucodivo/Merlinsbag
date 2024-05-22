package com.inasweaterpoorlyknit.core.database.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.inasweaterpoorlyknit.core.database.model.ArticleImageEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import kotlinx.coroutines.flow.Flow

data class EnsembleArticles(
  @Embedded val ensemble: EnsembleEntity,
  @Relation(
    parentColumn = "id",
    entityColumn = "article_id",
    associateBy = Junction(
      value = EnsembleArticleEntity::class,
      parentColumn = "ensemble_id",
      entityColumn = "article_id",
    )
  )
  val articles: List<ArticleImageEntity>
)

@Dao
interface EnsembleDao {
  @Insert
  fun insertEnsemble(vararg ensembleEntity: EnsembleEntity)
  @Update
  fun updateEnsemble(ensembleEntity: EnsembleEntity)
  @Insert
  fun insertArticleEnsemble(vararg ensembleArticleEntity: EnsembleArticleEntity)

  @Query("""DELETE FROM ensemble_article WHERE ensemble_id = :ensembleId AND article_id IN (:articleIds)""")
  fun deleteArticleEnsembles(ensembleId: String, articleIds: List<String>)

  @Transaction
  @Query(
    """SELECT ensemble_article.article_id as article_id FROM ensemble_article 
      JOIN article ON article.id = article_id
      WHERE ensemble_article.ensemble_id = :ensembleId"""
  )
  fun getEnsembleArticleImages(ensembleId: String): Flow<List<ArticleWithImages>>

  @Transaction
  @Query("""SELECT * FROM ensemble""")
  fun getAllEnsembleArticleImages(): Flow<List<EnsembleArticles>>

  @Query("SELECT * FROM ensemble")
  fun getAllEnsembles(): Flow<List<EnsembleEntity>>

  @Query(
    """ SELECT * FROM ensemble
        WHERE id = :ensembleId """
  )
  fun getEnsemble(ensembleId: String): Flow<EnsembleEntity>

  @Transaction
  fun insertEnsembleWithArticles(ensemble: EnsembleEntity, articleIds: List<String>){
    val ensembleArticles = Array(articleIds.size){ index ->
      EnsembleArticleEntity(ensembleId = ensemble.id, articleId = articleIds[index])
    }
    insertEnsemble(ensemble)
    insertArticleEnsemble(*ensembleArticles)
  }
}
