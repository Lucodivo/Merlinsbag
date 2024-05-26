package com.inasweaterpoorlyknit.core.repository.model

interface LazyUriStrings {
  companion object {
    val Empty = object : LazyUriStrings {
      override val size get() = 0
      override fun getUriString(index: Int): String = ""
    }
  }
  val size: Int
  fun isEmpty() = size == 0
  fun isNotEmpty() = size != 0
  fun getUriString(index: Int): String
}