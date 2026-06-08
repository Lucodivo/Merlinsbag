package com.inasweaterpoorlyknit.core.database.model

interface ArticleThumbnails {
  val articleId: String
  val thumbnailPaths: List<ThumbnailFilename>
}