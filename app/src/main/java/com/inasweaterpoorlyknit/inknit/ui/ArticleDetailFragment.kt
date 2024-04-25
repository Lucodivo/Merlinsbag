package com.inasweaterpoorlyknit.inknit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.compose.AsyncImage
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitTheme

class ArticleDetailFragment: Fragment() {
  val viewModel: ArticleDetailViewModel by viewModels()
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return ComposeView(requireContext()).apply {
      setContent {
        InKnitTheme {
          AsyncImage(model = "TODO: getURI", contentDescription = "TODO: image description")
        }
      }
    }
  }
}