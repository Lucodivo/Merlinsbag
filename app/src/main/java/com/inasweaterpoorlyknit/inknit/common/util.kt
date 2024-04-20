package com.inasweaterpoorlyknit.inknit.common

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    fun reset(){
        startNs = System.nanoTime()
        milestoneStartNs = startNs
    }
}

// Context extensions
fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun Fragment.toast(msg: String) = requireContext().toast(msg)
fun Context.createImageFileUri(): Uri? {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return try {
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        FileProvider.getUriForFile(this, "com.inasweaterpoorlyknit.inknit.fileprovider", file)
    } catch (ex: IOException) {
        Log.e("createImageFileUri", "Failed to create file - ${ex.message}")
        null
    }
}