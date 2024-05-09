package com.inasweaterpoorlyknit.core.database.repository

import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import kotlinx.coroutines.flow.Flow

class EnsembleRepository(
  private val ensembleDao: EnsembleDao
) {
  fun getAllEnsembles(): Flow<List<EnsembleEntity>> = ensembleDao.getAllEnsembles()
  fun insertEnsemble(title: String) = ensembleDao.insertEnsemble(EnsembleEntity(title = title))
}