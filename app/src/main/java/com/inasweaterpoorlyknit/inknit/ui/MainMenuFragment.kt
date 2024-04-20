package com.inasweaterpoorlyknit.inknit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.common.toast
import com.inasweaterpoorlyknit.inknit.ui.theme.InknitTheme

class MainMenuFragment: Fragment() {
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return ComposeView(requireContext()).apply {
      setContent {
        MainMenuScreen(
          onClickOutfits = { toast("Outfits") },
          onClickWardrobe = {
            val directions = MainMenuFragmentDirections.actionMainMenuFragmentToWardrobeFragment()
            findNavController().navigate(directions)
          },
          onClickAddArticles = {
            val directions = MainMenuFragmentDirections.actionMainMenuFragmentToAddArticleFragment()
            findNavController().navigate(directions)
          },
        )
      }
    }
  }
}

@Preview
@Composable
fun MainMenuScreen(
  onClickOutfits: () -> Unit = {},
  onClickWardrobe: () -> Unit = {},
  onClickAddArticles: () -> Unit = {},
) {
  InknitTheme {
    listOf(
      listOf(
        ImageWithTextData(R.drawable.jewlery_reyda_donmez, R.string.earrings, R.string.outfits, onClickOutfits),
      ),
      listOf(
        ImageWithTextData(R.drawable.shirt_reyda_donmez, R.string.shirt, R.string.wardrobe, onClickWardrobe),
      ),
      listOf(
        ImageWithTextData(R.drawable.camera_reyda_donmez, R.string.camera, R.string.digitize, onClickAddArticles),
      ),
    ).also { ImageWithTextGrid(buttonsTopToBottom = it) }
  }
}
