package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo

data class ImageFilenames(
    @ColumnInfo("filename") val filename: String,
    @ColumnInfo("filename_thumb") val filenameThumb: String,
)