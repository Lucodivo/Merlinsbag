package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "article")
data class ArticleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "created") val createdEpoch: Long = Date().time,
    @ColumnInfo(name = "modified") val modifiedEpoch: Long = createdEpoch,
)