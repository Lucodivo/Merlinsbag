package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import com.inasweaterpoorlyknit.core.model.ImageFilenames

data class ImageFilenamesQueryResponse(
    @ColumnInfo("filename") override val filename: String,
    @ColumnInfo("filename_thumb") override val filenameThumb: String,
): ImageFilenames