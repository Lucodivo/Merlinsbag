package com.inasweaterpoorlyknit.core.data

import android.content.Context
import android.os.Environment
import java.io.File

internal fun articleFilesDir(context: Context) = File(context.filesDir, "articles")
internal fun articleFilesDirStr(context: Context) = articleFilesDir(context).toString() + '/'
internal const val exportFolderName = "Merlinsbag"
internal val exportDir = "Pictures${File.separator}$exportFolderName"
internal val exportDirBeforeAndroidQ =
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() +
            File.separator + exportFolderName
