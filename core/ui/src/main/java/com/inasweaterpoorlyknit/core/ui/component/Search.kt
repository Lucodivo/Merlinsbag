package com.inasweaterpoorlyknit.core.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.core.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme

@Composable
fun SearchBox(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier,
){
  TextField(
    value = query,
    placeholder = { Text(placeholder) },
    onValueChange = onQueryChange,
    singleLine = true,
    leadingIcon = {
      Icon(
        imageVector = NoopIcons.Search,
        contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
        modifier = Modifier.padding(start = 4.dp)
      ) },
    shape = MaterialTheme.shapes.extraLarge,
    trailingIcon = {
      if(query.isNotEmpty()) {
        IconButton(
          onClick = onClearQuery,
          modifier = Modifier.padding(end = 4.dp)
        ){
          Icon(NoopIcons.Close, TODO_ICON_CONTENT_DESCRIPTION)
        }
      }
    },
    colors = TextFieldDefaults.colors().copy(
      focusedIndicatorColor = Color.Transparent,
      disabledIndicatorColor = Color.Transparent,
      unfocusedIndicatorColor = Color.Transparent,
    ),
    modifier = modifier
  )
}

@Composable
fun PreviewUtilSearchBox(query: String = "") = NoopTheme {
  SearchBox(query = query, placeholder = "Goth 2 Boss", onClearQuery = {}, onQueryChange = {})
}

@Preview @Composable fun PreviewSearchBox() = PreviewUtilSearchBox("Road Warrior")
@Preview @Composable fun PreviewSearchBoxEmpty() = PreviewUtilSearchBox()
