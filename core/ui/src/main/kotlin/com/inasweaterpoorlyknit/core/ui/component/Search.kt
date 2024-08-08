package com.inasweaterpoorlyknit.core.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.core.ui.R
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme

@Composable
fun NoopSearchBox(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier,
){
  val focusRequester = remember { FocusRequester() }
  val iconPadding = 4.dp
  TextField(
    value = query,
    placeholder = { Text(placeholder) },
    onValueChange = onQueryChange,
    singleLine = true,
    leadingIcon = { Icon(
          imageVector = NoopIcons.Search,
          contentDescription = stringResource(R.string.search),
          modifier = Modifier.padding(start = iconPadding)
    )},
    shape = MaterialTheme.shapes.extraLarge,
    trailingIcon = {
      if(query.isNotEmpty()) {
        IconButton(
          onClick = onClearQuery,
          modifier = Modifier.padding(end = iconPadding)
        ){
          Icon(NoopIcons.Close, stringResource(R.string.clear_query))
        }
      }
    },
    colors = TextFieldDefaults.colors().copy(
      focusedIndicatorColor = Color.Transparent,
      disabledIndicatorColor = Color.Transparent,
      unfocusedIndicatorColor = Color.Transparent,
    ),
    modifier = modifier.focusRequester(focusRequester)
  )
}

@Composable
fun PreviewUtilSearchBox(query: String = "") = NoopTheme {
  NoopSearchBox(query = query, placeholder = "Goth 2 Boss", onClearQuery = {}, onQueryChange = {})
}

@Preview @Composable fun PreviewSearchBox() = PreviewUtilSearchBox("Road Warrior")
@Preview @Composable fun PreviewSearchBoxEmpty() = PreviewUtilSearchBox()
