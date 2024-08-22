package com.inasweaterpoorlyknit.merlinsbag.devtool

//import com.inasweaterpoorlyknit.core.data.repository.articleFilesDir
//import com.inasweaterpoorlyknit.core.data.repository.saveBitmap
//import com.inasweaterpoorlyknit.core.data.repository.toThumbnail

// Convert article images to standard quality
// Will not build as it accesses private/internal functions
/*
suspend fun downscaleWebPArticleImages(context: Context) {
  val articleFilesDir = articleFilesDir(context).apply { mkdirs() }
  val imageQuality = ImageQuality.STANDARD
  articleFilesDir.listFiles()?.let { files ->
    val articleCount = files.size / 2
    var fileCount = 0
    files.forEachIndexed { index, file ->
      if(!file.path.endsWith("thumb.webp")){ // skip thumbnails
        fileCount++
        val fileNum = fileCount
        with(CoroutineScope(coroutineContext)){
          launch {
            val bitmap = BitmapFactory.decodeFile(file.path)
            val bitmapThumb = bitmap.toThumbnail()
            val fileName = file.name
            val thumbFileName = fileName.replaceFirst("_full.webp", "_thumb.webp")
            // Save full image with new compression rate
            saveBitmap(
              directory = articleFilesDir,
              fileName = fileName,
              bitmap = bitmap,
              compressionFormat = imageQuality.compressionFormat(),
              compressionQuality = imageQuality.compressionQuality(),
            )
            saveBitmap(
              directory = articleFilesDir,
              fileName = thumbFileName,
              bitmap = bitmapThumb,
              compressionFormat = compressionFormatThumb,
              compressionQuality = compressionQualityThumb,
            )
            Log.i("WebP conversions", "$fileNum / $articleCount : $fileName")
          }
        }
      }
    }
  }
  Log.i("WebP conversions", "All webp conversions dispatched")
}
*/
