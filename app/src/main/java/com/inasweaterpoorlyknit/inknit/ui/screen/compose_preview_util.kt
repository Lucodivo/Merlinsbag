package com.inasweaterpoorlyknit.inknit.ui.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
fun previewAssetBitmap(filename: String): Bitmap = BitmapFactory.decodeStream(LocalContext.current.assets.open(filename))

@Preview(name = "phone", device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
@Preview(name = "landscape", device = "spec:shape=Normal,width=640,height=360,unit=dp,dpi=480")
@Preview(name = "foldable", device = "spec:shape=Normal,width=673,height=841,unit=dp,dpi=480")
@Preview(name = "tablet", device = "spec:shape=Normal,width=1280,height=800,unit=dp,dpi=480")
annotation class DevicePreviews

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun currentWindowAdaptiveInfo(): WindowSizeClass {
  val configuration = LocalConfiguration.current
  val size = DpSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
  return WindowSizeClass.calculateFromSize(size)
}