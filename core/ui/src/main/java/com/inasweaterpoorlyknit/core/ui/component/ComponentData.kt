package com.inasweaterpoorlyknit.core.ui.component

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class IconButtonData(val icon: IconData, val enabled: Boolean = true, val onClick: () -> Unit)

data class IconData(val icon: ImageVector, val contentDescription: String) {
  val asComposable: @Composable () -> Unit = { Icon(imageVector = icon, contentDescription = contentDescription) }
}