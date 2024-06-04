package com.inasweaterpoorlyknit.merlinsbag.ui.component

import androidx.compose.ui.graphics.vector.ImageVector

data class TextButtonData(val icon: IconData, val onClick: () -> Unit)
data class IconData(val icon: ImageVector, val contentDescription: String)