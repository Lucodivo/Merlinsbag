package com.inasweaterpoorlyknit.inknit.ui.screen

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.platform.LocalView
import com.inasweaterpoorlyknit.inknit.ui.hideSystemUI
import com.inasweaterpoorlyknit.inknit.ui.showSystemUI

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
