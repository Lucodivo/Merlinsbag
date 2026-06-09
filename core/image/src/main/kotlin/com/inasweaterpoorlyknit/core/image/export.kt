package com.inasweaterpoorlyknit.core.image

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


fun exportImage(context: Context, imageUri: String): Uri? {
    val bitmapToExport = BitmapFactory.decodeFile(imageUri)

    val exportFilname = imageUri.replace(".webp", ".png").substringAfterLast("/")

    var exportUri: Uri? = null
    var success = false
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, exportFilname)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, exportDir)
        // TODO: MediaStore.MediaColumns.IN_PROGRESS
        exportUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        exportUri?.let { uri ->
            resolver.openOutputStream(uri)?.let { fileOutputStream ->
                bitmapToExport.compress(imageExportFormat, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                success = true
            }
        }
    } else {
        val exportFile = File(exportDirBeforeAndroidQ, exportFilname)
        try {
            FileOutputStream(exportFile).let { fileOutputStream ->
                bitmapToExport.compress(imageExportFormat, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
            }
            MediaScannerConnection.scanFile(context, arrayOf(exportFile.toString()), arrayOf("image/png"), null)
            exportUri = Uri.fromFile(exportFile)
            success = true
        } catch(e: IOException){
            Log.e("Export Article", "${e.message}\nFailed to copy article image to public directory: $imageUri -> ${exportFile.path}")
        }
    }

    return if(success) exportUri else null
}
