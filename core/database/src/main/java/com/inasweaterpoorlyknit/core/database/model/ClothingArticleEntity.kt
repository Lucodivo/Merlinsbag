package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "clothing_articles")
data class ClothingArticleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "created") val createdEpoch: Long = Date().time,
    @ColumnInfo(name = "modified") val modifiedEpoch: Long = createdEpoch,
    // TODO: Categories
)