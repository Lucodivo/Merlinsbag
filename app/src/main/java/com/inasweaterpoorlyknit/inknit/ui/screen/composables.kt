package com.inasweaterpoorlyknit.inknit.ui.screen

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.inasweaterpoorlyknit.inknit.ui.hideSystemUI
import com.inasweaterpoorlyknit.inknit.ui.showSystemUI
import com.inasweaterpoorlyknit.inknit.ui.toast

@Composable
@NonRestartableComposable
fun HideSystemUIDisposableEffect() {
  val activity = LocalView.current.context as Activity
  DisposableEffect(Unit) {
    activity.window.hideSystemUI()
    onDispose {
      activity.window.showSystemUI()
    }
  }
}

@Composable
fun Toast(@StringRes msg: Int) = LocalContext.current.toast(msg)