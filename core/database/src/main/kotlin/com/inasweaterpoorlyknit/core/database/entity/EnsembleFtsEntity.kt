package com.inasweaterpoorlyknit.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "ensemble_fts")
@Fts4(contentEntity = EnsembleEntity::class)
data class EnsembleFtsEntity(
    @ColumnInfo(name = "id") val ensembleId: String,
    @ColumnInfo(name = "title") val title: String,
)