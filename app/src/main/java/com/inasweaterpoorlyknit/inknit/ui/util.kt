package com.inasweaterpoorlyknit.inknit.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment

fun Fragment.showSystemUI() = requireActivity().showSystemUI()
fun Fragment.hideSystemUI() = requireActivity().hideSystemUI()

fun Activity.showSystemUI() {
  if(Build.VERSION.SDK_INT >= 30) {
    window.apply {
      setDecorFitsSystemWindows(true)
      // TODO: What is the status bar color used for the app?
      statusBarColor = Color.BLACK
    }
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
      controller.show(WindowInsetsCompat.Type.systemBars())
      controller.show(WindowInsetsCompat.Type.navigationBars())
    }
  } else { // TODO: Test if this even works
    @Suppress("DEPRECATION")
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
  }
}

fun Activity.hideSystemUI() {
  if(Build.VERSION.SDK_INT >= 30) {
    window.apply {
      setDecorFitsSystemWindows(false) // fill window
      statusBarColor = Color.TRANSPARENT // set
    }

    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
      // hide navigation buttons
      controller.hide(WindowInsetsCompat.Type.systemBars())
      // allow navbar to show up after swipe
      controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
  } else { // TODO: System visibility is deprecated, remove when minSDK is 30+
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

fun Context.getActivity(): ComponentActivity? = when (this) {
  is ComponentActivity -> this
  is ContextWrapper -> baseContext.getActivity()
  else -> null
}
