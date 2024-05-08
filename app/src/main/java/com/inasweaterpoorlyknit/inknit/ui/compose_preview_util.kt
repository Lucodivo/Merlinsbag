package com.inasweaterpoorlyknit.inknit.ui

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.RawRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.Rotate90DegreesCcw
import androidx.compose.material.icons.outlined.Rotate90DegreesCw
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.SwitchLeft
import androidx.compose.material.icons.outlined.SwitchRight
import androidx.compose.material.icons.outlined.ZoomInMap
import androidx.compose.material.icons.outlined.ZoomOutMap
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
val COMPOSE_PREVIEW_CONTENT_DESCRIPTION = "compose preview content description"

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

/*
 Switching icons for app shouldn't always change previews
 Some previews simply demonstrate the essence of a component
 And simply require *any* icon
*/
object NoopComposePreviewIcons {
  val PhotoAlbum = Icons.Filled.PhotoAlbum
  val AddAPhoto = Icons.Filled.AddAPhoto
  val Remove = Icons.Filled.Remove
  val Add = Icons.Filled.Add
  val Save = Icons.Outlined.Save
  val Check = Icons.Outlined.Check
  val Category = Icons.Outlined.Category
  val CategoryFilled = Icons.Filled.Category
  val FolderSpecial = Icons.Outlined.FolderSpecial
  val FolderSpecialFilled = Icons.Filled.FolderSpecial
}
