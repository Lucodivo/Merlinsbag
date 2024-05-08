package com.inasweaterpoorlyknit.inknit.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopTheme

@Composable
fun NoopDialog(){
  Dialog(onDismissRequest = {}) {
  }
}


@Preview
@Composable
fun PreviewNoopDialog() {
  NoopTheme {
    NoopDialog()
  }
}