package com.inasweaterpoorlyknit.core.model

interface LazyFilenames {
  val lazyFullImageUris: LazyUriStrings
  val lazyThumbImageUris: LazyUriStrings
  val size: Int
  fun isEmpty() = size == 0
  fun isNotEmpty() = size != 0

  companion object {
    val Empty = object: LazyFilenames {
      override val lazyFullImageUris: LazyUriStrings = LazyUriStrings.Empty
      override val lazyThumbImageUris: LazyUriStrings = LazyUriStrings.Empty
      override val size: Int = 0
    }
  }
}