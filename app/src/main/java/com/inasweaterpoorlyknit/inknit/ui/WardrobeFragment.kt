package com.inasweaterpoorlyknit.inknit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.common.toast
import com.inasweaterpoorlyknit.inknit.ui.theme.InknitTheme

class WardrobeFragment : Fragment() {
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return ComposeView(requireContext()).apply {
      setContent {
        WardrobeScreen(
          onClickOnePieces = { toast("One-piece") },
          onClickEyewear = { toast("Glasses") },
          onClickAccessories = { toast("Accessory") },
          onClickHats = { toast("Hat") },
          onClickShoes = { toast("Shoe") },
          onClickOuterwear = { toast("Sweater") },
          onClickBottoms = { toast("Skirt") },
          onClickTops = { toast("Shirt") },
        )
      }
    }
  }
}

@Preview
@Composable
fun WardrobeScreen(
  onClickEyewear: () -> Unit = {},
  onClickAccessories: () -> Unit = {},
  onClickHats: () -> Unit = {},
  onClickShoes: () -> Unit = {},
  onClickOnePieces: () -> Unit = {},
  onClickOuterwear: () -> Unit = {},
  onClickBottoms: () -> Unit = {},
  onClickTops: () -> Unit = {},
) {
  InknitTheme {
    listOf(
      listOf(
        ImageWithTextData(R.drawable.glasses_2_reyda_donmez, R.string.glasses, R.string.eyewear, onClickEyewear),
        ImageWithTextData(R.drawable.necklace_reyda_donmez, R.string.heart_necklace, R.string.accessories, onClickAccessories),
      ),
      listOf(
        ImageWithTextData(R.drawable.crown_reyda_donmez, R.string.crown, R.string.hats, onClickHats),
        ImageWithTextData(R.drawable.running_shoe_reyda_donmez, R.string.shoe, R.string.shoes, onClickShoes),
      ),
      listOf(
        ImageWithTextData(R.drawable.swimsuit_reyda_donmez, R.string.swimsuit, R.string.one_pieces, onClickOnePieces),
        ImageWithTextData(R.drawable.sweater_reyda_donmez, R.string.sweater, R.string.outerwear, onClickOuterwear),
      ),
      listOf(
        ImageWithTextData(R.drawable.skirt_reyda_donmez, R.string.skirt, R.string.bottoms, onClickBottoms),
        ImageWithTextData(R.drawable.shirt_reyda_donmez, R.string.shirt, R.string.tops, onClickTops),
      ),
    ).also { ImageWithTextGrid(buttonsTopToBottom = it) }
  }
}