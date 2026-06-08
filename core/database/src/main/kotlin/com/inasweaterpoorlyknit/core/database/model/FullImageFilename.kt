package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo

data class FullImageFilename(
    @ColumnInfo("filename") val filename: String,
)