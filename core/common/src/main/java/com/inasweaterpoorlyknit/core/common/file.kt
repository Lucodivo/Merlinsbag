package com.inasweaterpoorlyknit.core.common

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
fun timestampFileName(): String = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
fun articleFilesDir(context: Context) = File(context.filesDir, "articles")
fun articleFilesDirStr(context: Context) = articleFilesDir(context).toString()