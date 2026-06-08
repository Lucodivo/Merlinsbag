package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo

data class Ensemble(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title") val title: String,
)