package com.inasweaterpoorlyknit.core.testing

import android.net.Uri
import java.util.UUID

fun randUUIDString() = UUID.randomUUID().toString()
object Counter{
  private var count = 0L
  fun next(): Long = count++
}

fun createFakeUriString() = "fakeimage${Counter.next()}"
fun createFakeUriStrings(count: Int) = Array(count){ createFakeUriString() }
fun createFakeUri(): Uri = Uri.parse(createFakeUriString())
fun createFakeUris(count: Int) = Array(count){ createFakeUri() }
