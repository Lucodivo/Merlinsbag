package com.inasweaterpoorlyknit.inknit.ui.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun previewAssetBitmap(filename: String): Bitmap = BitmapFactory.decodeStream(LocalContext.current.assets.open(filename))