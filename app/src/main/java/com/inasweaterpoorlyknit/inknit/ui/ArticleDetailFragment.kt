package com.inasweaterpoorlyknit.inknit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import coil.compose.AsyncImage
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitTheme
import com.inasweaterpoorlyknit.inknit.viewmodels.ArticleDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArticleDetailFragment: Fragment() {
  private val args: ArticleDetailFragmentArgs by navArgs()
  private val viewModel: ArticleDetailViewModel by viewModels()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return ComposeView(requireContext()).apply {
      setContent {
        val clothingDetail = viewModel.getArticleDetails(args.clothingArticleId).observeAsState(initial = null)
        InKnitTheme {
          AsyncImage(model = clothingDetail.value?.imageUriString, contentDescription = "TODO: image description")
        }
      }
    }
  }
}

@Composable
fun ArticleDetailScreen(
  modifier: Modifier = Modifier,
  imageUriString: String?) {
  AsyncImage(model = imageUriString, contentDescription = "TODO: image description")
}