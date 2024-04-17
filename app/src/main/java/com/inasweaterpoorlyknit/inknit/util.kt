package com.inasweaterpoorlyknit.inknit

import android.content.Context
import android.util.Log
import android.widget.Toast

const val NANOSECONDS_PER_MICROSECOND = 1_000
const val NANOSECONDS_PER_MILLISECOND = 1_000_000
const val NANOSECONDS_PER_SECOND = 1_000_000_000
fun nanosecondsToString(nanoseconds: Long): String  {
    return when(nanoseconds) {
        in 0..<1_000 -> { "${nanoseconds}ns" }
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
    fun elapsedNs(): Long = System.nanoTime() - startNs
    fun logElapsed(tag: String) {
        val elapsedNs = elapsedNs()
        Log.i("InKnit", "$tag: ${nanosecondsToString(elapsedNs)}")
    }
    fun reset(){ startNs = System.nanoTime() }
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
