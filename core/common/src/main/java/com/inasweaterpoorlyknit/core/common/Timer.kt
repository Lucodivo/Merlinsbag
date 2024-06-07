package com.inasweaterpoorlyknit.core.common

import android.util.Log

const val NANOSECONDS_PER_MICROSECOND = 1_000
const val NANOSECONDS_PER_MILLISECOND = 1_000_000
const val NANOSECONDS_PER_SECOND = 1_000_000_000
fun nanosecondsToString(nanoseconds: Long): String {
  return when(nanoseconds) {
    in 0..<1_000 -> {
      "${nanoseconds}ns"
    }

    in 0..<1_000_000 -> {
      val microseconds = nanoseconds.toDouble() / NANOSECONDS_PER_MICROSECOND.toDouble()
      "${"%.2f".format(microseconds)}µs"
    }

    in 0..<1_000_000_000 -> {
      val milliseconds = nanoseconds.toDouble() / NANOSECONDS_PER_MILLISECOND.toDouble()
      "${"%.2f".format(milliseconds)}ms"
    }

    else -> {
      val seconds = nanoseconds.toDouble() / NANOSECONDS_PER_SECOND.toDouble()
      "${"%.2f".format(seconds)}s"
    }
  }
}

class Timer {
  var startNs = System.nanoTime()
  var milestoneStartNs = startNs
  fun totalElapsedNs(): Long = System.nanoTime() - startNs
  fun milestoneElapsedNS(): Long {
    val result = System.nanoTime() - milestoneStartNs
    milestoneStartNs = System.nanoTime()
    return result
  }

  fun logMilestone(tag: String, msg: String) {
    Log.i(tag, "$msg: ${nanosecondsToString(milestoneElapsedNS())}")
  }

  fun logElapsed(tag: String, msg: String) {
    Log.i(tag, "$msg: ${nanosecondsToString(totalElapsedNs())}")
  }

  fun reset() {
    startNs = System.nanoTime()
    milestoneStartNs = startNs
  }
}