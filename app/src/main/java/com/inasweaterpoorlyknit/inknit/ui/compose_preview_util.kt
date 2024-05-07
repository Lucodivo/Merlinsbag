package com.inasweaterpoorlyknit.inknit.ui

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.RawRes
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.inknit.R


val longArticle = "long_compose_preview.webp"
val squareishComposable = "add_article_compose_preview.webp"

@Composable
fun previewAssetBitmap(filename: String): Bitmap = BitmapFactory.decodeStream(LocalContext.current.assets.open(filename))
fun previewAssetBitmap(filename: String, context: Context): Bitmap = BitmapFactory.decodeStream(context.assets.open(filename))

@Composable
fun resourceAsUriString(@RawRes resId: Int): String {
  val resources = LocalContext.current.resources
  return Uri.parse(
    ContentResolver.SCHEME_ANDROID_RESOURCE +
        "://" + resources.getResourcePackageName(resId)
        + '/' + resources.getResourceTypeName(resId)
        + '/' + resources.getResourceEntryName(resId)
  ).toString()
}

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

val isComposePreview
  @Composable get() = LocalInspectionMode.current

val allTestThumbnailResourceIdsAsStrings = arrayOf(
    R.raw.test_thumb_1.toString(), R.raw.test_thumb_2.toString(), R.raw.test_thumb_3.toString(),
    R.raw.test_thumb_4.toString(), R.raw.test_thumb_5.toString(), R.raw.test_thumb_6.toString(),
    R.raw.test_thumb_7.toString(), R.raw.test_thumb_8.toString(), R.raw.test_thumb_9.toString(),
)
val repeatedThumbnailResourceIdsAsStrings = arrayListOf(*allTestThumbnailResourceIdsAsStrings, *allTestThumbnailResourceIdsAsStrings, *allTestThumbnailResourceIdsAsStrings)