package com.inasweaterpoorlyknit.core.ui

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.RawRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.core.model.LazyUriStrings


const val composePreviewArticleAsset = "compose_preview_article_full.webp"
const val COMPOSE_PREVIEW_CONTENT_DESCRIPTION = "compose preview content description"
const val COMPOSE_ID = "compose preview id"

@Composable
fun previewAssetBitmap(filename: String): Bitmap = BitmapFactory.decodeStream(LocalContext.current.assets.open(filename))
fun previewAssetBitmap(filename: String, context: Context): Bitmap = BitmapFactory.decodeStream(context.assets.open(filename))

@Composable
fun resourceAsUriString(@RawRes resId: Int): String {
  val resources = LocalContext.current.resources
  return Uri.parse(
    ContentResolver.SCHEME_ANDROID_RESOURCE
    + "://" + resources.getResourcePackageName(resId)
    + '/' + resources.getResourceTypeName(resId)
    + '/' + resources.getResourceEntryName(resId)
  ).toString()
}

@Preview(name = "phone", device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
@Preview(name = "landscape", device = "spec:shape=Normal,width=640,height=360,unit=dp,dpi=480")
@Preview(name = "foldable", device = "spec:shape=Normal,width=673,height=841,unit=dp,dpi=480")
@Preview(name = "tablet", device = "spec:shape=Normal,width=1280,height=800,unit=dp,dpi=480")
annotation class DevicePreviews

@Preview(name = "landscape", device = "spec:shape=Normal,width=640,height=360,unit=dp,dpi=480")
annotation class LandscapePreview

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun currentWindowAdaptiveInfo(): WindowSizeClass {
  val configuration = LocalConfiguration.current
  val size = DpSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
  return WindowSizeClass.calculateFromSize(size)
}

val isComposePreview
  @Composable get() = LocalInspectionMode.current

val allTestFullResourceIdsAsStrings = arrayOf(
  R.raw.test_full_1.toString(), R.raw.test_full_2.toString(), R.raw.test_full_3.toString(),
  R.raw.test_full_4.toString(), R.raw.test_full_5.toString(), R.raw.test_full_6.toString(),
  R.raw.test_full_7.toString(), R.raw.test_full_8.toString(), R.raw.test_full_9.toString(),
)
val repeatedFullResourceIdsAsStrings = arrayListOf(*allTestFullResourceIdsAsStrings, *allTestFullResourceIdsAsStrings, *allTestFullResourceIdsAsStrings)
val allTestThumbnailResourceIdsAsStrings = arrayOf(
  R.raw.test_thumb_1.toString(), R.raw.test_thumb_2.toString(), R.raw.test_thumb_3.toString(),
  R.raw.test_thumb_4.toString(), R.raw.test_thumb_5.toString(), R.raw.test_thumb_6.toString(),
  R.raw.test_thumb_7.toString(), R.raw.test_thumb_8.toString(), R.raw.test_thumb_9.toString(),
)
val repeatedThumbnailResourceIdsAsStrings = arrayListOf(*allTestThumbnailResourceIdsAsStrings, *allTestThumbnailResourceIdsAsStrings, *allTestThumbnailResourceIdsAsStrings)
val lazyRepeatedThumbnailResourceIdsAsStrings =
    object : LazyUriStrings {
      override val size: Int = repeatedThumbnailResourceIdsAsStrings.size
      override fun getUriString(index: Int): String = repeatedThumbnailResourceIdsAsStrings[index]
    }
val repeatedThumbnailResourceIdsAsStrings_EveryOtherIndexSet = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet()

/*
 Switching icons for app shouldn't always change previews
 Some previews simply demonstrate the essence of a component
 And simply require *any* icon
*/
object NoopComposePreviewIcons {
  val AddPhotoAlbum = Icons.Filled.AddPhotoAlternate
  val AddPhotoCamera = Icons.Filled.AddAPhoto
  val Remove = Icons.Filled.Remove
  val Add = Icons.Filled.Add
  val Edit = Icons.Filled.Edit
  val Settings = Icons.Filled.Settings
  val Save = Icons.Outlined.Save
  val Check = Icons.Outlined.Check
  val Category = Icons.Outlined.Category
  val CategoryFilled = Icons.Filled.Category
  val FolderSpecial = Icons.Outlined.FolderSpecial
  val FolderSpecialFilled = Icons.Filled.FolderSpecial
}
