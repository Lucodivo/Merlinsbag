package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo

data class ThumbnailFilename(
    @ColumnInfo("filename_thumb") val filenameThumb: String,
)