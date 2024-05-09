package com.inasweaterpoorlyknit.core.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.inasweaterpoorlyknit.core.database.model.ArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ArticleEnsembleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity

@Dao
interface ArticleEnsembleDao {
  @Insert
  fun insertEnsemble(vararg ensembleEntity: EnsembleEntity)
  @Update
  fun updateEnsemble(ensembleEntity: EnsembleEntity)

  @Insert
  fun insertArticleEnsemble(vararg articleEnsembleEntity: ArticleEnsembleEntity)

  @Query(
    """ SELECT article.*
                FROM article
                JOIN article_ensemble ON article.id = article_ensemble.article_id
                WHERE article_ensemble.ensemble_id = :ensembleId """
  )
  fun getAllEnsembleArticles(ensembleId: String): LiveData<List<ArticleEntity>>
}
