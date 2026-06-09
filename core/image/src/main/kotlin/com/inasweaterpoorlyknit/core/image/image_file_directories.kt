package com.inasweaterpoorlyknit.core.image

import android.content.Context
import android.os.Environment
import java.io.File

fun articleFilesDir(context: Context) = File(context.filesDir, "articles")
fun articleFilesDirStr(context: Context) = articleFilesDir(context).toString() + '/'
const val exportFolderName = "Merlinsbag"
val exportDir = "Pictures${File.separator}$exportFolderName"
val exportDirBeforeAndroidQ =
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() +
            File.separator + exportFolderName
