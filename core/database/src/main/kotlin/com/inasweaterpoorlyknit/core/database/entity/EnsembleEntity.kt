package com.inasweaterpoorlyknit.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "ensemble",
  indices = [Index(value = ["title"], unique = true)])
data class EnsembleEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "created") val created: Long,
    @ColumnInfo(name = "modified") val modified: Long,
)