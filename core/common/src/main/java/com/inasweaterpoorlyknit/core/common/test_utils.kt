package com.inasweaterpoorlyknit.core.common

import android.net.Uri
import java.util.UUID

fun randUUIDString() = UUID.randomUUID().toString()
object Counter{
  private var count = 0
  fun next(): Int = count++
}

fun createFakeUriString() = "content://com.inasweaterpoorlyknit.inknit/fakeimage${Counter.next()}"
fun createFakeUriStrings(count: Int) = Array(count){ createFakeUriString() }
fun createFakeUri(): Uri = Uri.parse(createFakeUriString())
fun createFakeUris(count: Int) = Array(count){ createFakeUri() }
