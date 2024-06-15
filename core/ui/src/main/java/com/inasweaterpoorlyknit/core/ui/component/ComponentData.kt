package com.inasweaterpoorlyknit.core.ui.component

import androidx.compose.ui.graphics.vector.ImageVector

data class IconData(val icon: ImageVector, val contentDescription: String)
data class IconButtonData(val icon: IconData, val enabled: Boolean = true, val onClick: () -> Unit)