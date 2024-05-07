package com.inasweaterpoorlyknit.inknit.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.text.SimpleDateFormat
import java.util.Locale

fun pixelsToDp(pixels: Int) = (pixels / Resources.getSystem().displayMetrics.density).dp

fun Context.getActivity(): ComponentActivity? = when (this) {
  is ComponentActivity -> this
  is ContextWrapper -> baseContext.getActivity()
  else -> null
}
fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun Context.toast(@StringRes msg: Int) = Toast.makeText(this, resources.getString(msg), Toast.LENGTH_SHORT).show()

const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
fun timestampFileName(): String = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

fun Float.degToRad() = this * Math.PI / 180.0
fun Float.radToDeg() = this * 180.0 / Math.PI
fun Context.showSystemUI() {
  val window = (this as Activity).window
  if (Build.VERSION.SDK_INT >= 30) {
    window.setDecorFitsSystemWindows(true)
    WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
  } else {
    @Suppress("DEPRECATION")
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
  }
}

fun Context.hideSystemUI(){
  val window = (this as Activity).window
  if (Build.VERSION.SDK_INT >= 30) {
    window.setDecorFitsSystemWindows(false) // fill window
    with(WindowInsetsControllerCompat(window, window.decorView)){
      hide(WindowInsetsCompat.Type.systemBars())
      systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
  } else {
    @Suppress("DEPRECATION")
    window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or // hide the navigation
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or // lay out view as if the navigation will be hidden
                View.SYSTEM_UI_FLAG_IMMERSIVE or // used with HIDE_NAVIGATION to remain interactive when hiding navigation
                View.SYSTEM_UI_FLAG_FULLSCREEN or // fullscreen
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or // lay out view as if fullscreen
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE) // stable view of content (layout view size doesn't change)
  }
}