package com.inasweaterpoorlyknit.inknit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import coil.compose.AsyncImage
import com.inasweaterpoorlyknit.inknit.InKnitApplication
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitTheme

class ArticleDetailFragment: Fragment() {
  private val args: ArticleDetailFragmentArgs by navArgs()
  private lateinit var viewModel: ArticleDetailViewModel

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val viewModelFactory = ArticleDetailViewModelFactory(requireActivity().application as InKnitApplication, args.clothingArticleId)
    viewModel = ViewModelProvider(this, viewModelFactory).get(ArticleDetailViewModel::class.java)
    return ComposeView(requireContext()).apply {
      setContent {
        val clothingDetail = viewModel.getArticleDetails().observeAsState(initial = null)
        InKnitTheme {
          AsyncImage(model = clothingDetail.value?.imageUriString, contentDescription = "TODO: image description")
        }
      }
    }
  }
}