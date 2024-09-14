package com.inasweaterpoorlyknit.core.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import kotlin.math.min

@Composable
fun NoopSearchBox(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier,
    maxQueryLength: Int = Int.MAX_VALUE,
){
  val focusRequester = remember { FocusRequester() }
  val iconPadding = 4.dp
  TextField(
    value = query,
    placeholder = { Text(placeholder) },
    onValueChange = { value ->
      onQueryChange(value.substring(0..(min(maxQueryLength, value.lastIndex))))
    },
    singleLine = true,
    leadingIcon = {
      Icon(
        imageVector = NoopIcons.Search,
        contentDescription = stringResource(R.string.search),
        modifier = Modifier.padding(start = iconPadding)
      )
    },
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

// Search box that maintains the query
@Composable
fun NoopSearchBox(
    placeholder: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxQueryLength: Int = Int.MAX_VALUE,
){
  val (query, setQuery) = rememberSaveable { mutableStateOf("") }

  // Ensures the initial query is reported
  // This initial value may be recovered, via rememberSaveable,
  // after the Android OS terminates the app when it is in
  // the background (to repurpose the app's memory)
  LaunchedEffect(query) { onQueryChange(query) }

  NoopSearchBox(
    query = query,
    placeholder = placeholder,
    onQueryChange = setQuery,
    onClearQuery = { setQuery("") },
    modifier = modifier,
    maxQueryLength = maxQueryLength,
  )
}

@Composable
fun PreviewUtilSearchBox(query: String = "") = NoopTheme {
  NoopSearchBox(query = query, placeholder = "Goth 2 Boss", onClearQuery = {}, onQueryChange = {})
}

@Preview @Composable fun PreviewSearchBox() = PreviewUtilSearchBox("Road Warrior")
@Preview @Composable fun PreviewSearchBoxEmpty() = PreviewUtilSearchBox()
