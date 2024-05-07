package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.inasweaterpoorlyknit.inknit.ui.toast

@Composable
fun Toast(@StringRes msg: Int) = LocalContext.current.toast(msg)