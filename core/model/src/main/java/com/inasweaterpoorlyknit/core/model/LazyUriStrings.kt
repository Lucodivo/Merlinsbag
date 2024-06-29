package com.inasweaterpoorlyknit.core.model

// Images are stored as filenames in specified directories.
// This interface is used to reconstruct the absolute file path only as needed.
// There exists real user scenarios where we may pre-emptively retrieve 100s
// of image filenames but only 3-4 are ever actually accessed.
interface LazyUriStrings {
  companion object {
    val Empty = object : LazyUriStrings {
      override val size get() = 0
      override fun getUriString(index: Int): String = ""
      override fun removeAt(removedIndex: Int){}
    }
  }
  fun isEmpty() = size == 0
  fun isNotEmpty() = size != 0

  val size: Int
  fun getUriString(index: Int): String
  fun removeAt(removedIndex: Int)
}