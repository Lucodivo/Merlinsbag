package com.inasweaterpoorlyknit.core.repository.model

class LazyEnsembleThumbnails(
  val id: String,
  val title: String,
  val thumbnails: LazyArticleThumbnails,
)