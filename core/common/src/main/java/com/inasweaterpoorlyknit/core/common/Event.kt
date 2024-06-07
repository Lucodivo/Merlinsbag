package com.inasweaterpoorlyknit.core.common

// Helps avoid events from being handled multiple times after reconfiguration
open class Event<out T>(private val content: T?) {
  var hasBeenHandled = false
    private set

  fun getContentIfNotHandled(): T? {
    return if(!hasBeenHandled) {
      hasBeenHandled = true
      content
    } else null
  }
}