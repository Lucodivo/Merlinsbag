package com.inasweaterpoorlyknit.inknit.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.inknit.ui.theme.Shapes

data class ImageWithTextData(
  @DrawableRes val drawableId: Int,
  @StringRes val drawableDescriptionId: Int,
  @StringRes val text: Int,
  val onClick: () -> Unit = {},
)

@Composable
fun ImageWithText(
  menuItemData: ImageWithTextData,
  modifier: Modifier = Modifier,
) {
  Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = Shapes.large, onClick = menuItemData.onClick, modifier = modifier.fillMaxSize().padding(10.dp)){
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.padding(10.dp).fillMaxSize()
    ){
      Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(painter = painterResource(id = menuItemData.drawableId),
          contentDescription = stringResource(menuItemData.drawableDescriptionId),
          colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onPrimaryContainer),
          modifier = Modifier.weight(1f)
        )
        Text(text = stringResource(id = menuItemData.text))
      }
    }
  }
}

@Composable
fun ImageWithTextRow(buttonsLeftToRight: List<ImageWithTextData>, modifier: Modifier = Modifier) {
  Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = modifier.fillMaxWidth().padding(0.dp)) {
    buttonsLeftToRight.forEach {
      ImageWithText(it, modifier = Modifier.weight(1.0f))
    }
  }
}

@Composable
fun ImageWithTextGrid(buttonsTopToBottom: List<List<ImageWithTextData>>, modifier: Modifier = Modifier){
  Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = modifier.fillMaxWidth().padding(0.dp)) {
    buttonsTopToBottom.forEach { ImageWithTextRow(it, modifier = modifier.weight(1.0f)) }
  }
}