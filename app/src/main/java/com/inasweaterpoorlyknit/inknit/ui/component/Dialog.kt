package com.inasweaterpoorlyknit.inknit.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.inknit.ui.theme.Shapes

data class DialogUserData(
  val title: String,
)

/*
  Heavily based off of the Material 3 specks for fullscreen dialog:
     https://m3.material.io/components/dialogs/specs
 */
@Composable
fun NoopAddCollectionDialog(
  modifier: Modifier = Modifier,
  onPositive: (DialogUserData) -> Unit,
  onClose: () -> Unit,
) {
  val headerHeight = 56.dp
  val padding = 16.dp
  val (userInputTitle, setUserInputTitle) = remember { mutableStateOf("") }
  Surface(
    modifier = Modifier
      .fillMaxWidth(),
    shape = Shapes.medium,
  ) {
    Column(
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .height(headerHeight),
      ) {
        Row(
          horizontalArrangement = Arrangement.Start,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = NoopIcons.Close,
            contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
            modifier = Modifier
              .size(headerHeight)
              .padding(padding)
              .clickable{ onClose() }
          )
          Text(
            text = stringResource(id = R.string.Add_article_collection),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
          )
        }
        Text(
          text = stringResource(id = R.string.Save),
          color = MaterialTheme.colorScheme.primary,
          fontSize = MaterialTheme.typography.labelLarge.fontSize,
          textAlign = TextAlign.End,
          modifier = Modifier
            .height(headerHeight)
            .padding(padding)
            .clickable{
              onPositive(
                DialogUserData(
                  title = userInputTitle
                )
              )
            }
        )
      }
      Row{
        OutlinedTextField(
          value = userInputTitle,
          placeholder = { Text(text = stringResource(id = R.string.Goth_2_Boss)) },
          onValueChange = { setUserInputTitle(it) },
          label = { Text(text = stringResource(id = R.string.Collection_title)) },
        )
      }
      Spacer(modifier = Modifier.height(padding))
    }
  }
}

@Preview
@Composable
fun PreviewNoopDialog() {
  NoopTheme {
    NoopAddCollectionDialog(onPositive = {}, onClose = {})
  }
}