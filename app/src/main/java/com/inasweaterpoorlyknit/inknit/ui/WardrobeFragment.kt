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
          onClickSingle = { toast("Makeup") },
          onClickGlasses = { toast("Glasses") },
          onClickAccessory = { toast("Accessory") },
          onClickHat = { toast("Hat") },
          onClickShoe = { toast("Shoe") },
          onClickSweater = { toast("Sweater") },
          onClickSkirt = { toast("Skirt") },
          onClickShirt = { toast("Shirt") },
        )
      }
    }
  }
}

@Preview
@Composable
fun WardrobeScreen(
  onClickGlasses: () -> Unit = {},
  onClickAccessory: () -> Unit = {},
  onClickHat: () -> Unit = {},
  onClickShoe: () -> Unit = {},
  onClickSingle: () -> Unit = {},
  onClickSweater: () -> Unit = {},
  onClickSkirt: () -> Unit = {},
  onClickShirt: () -> Unit = {},
) {
  InknitTheme {
    listOf(
      listOf(
        ImageWithTextData(R.drawable.glasses_2_reyda_donmez, R.string.glasses, R.string.eyewear, onClickGlasses),
        ImageWithTextData(R.drawable.necklace_reyda_donmez, R.string.heart_necklace, R.string.accessories, onClickAccessory),
      ),
      listOf(
        ImageWithTextData(R.drawable.crown_reyda_donmez, R.string.crown, R.string.hats, onClickHat),
        ImageWithTextData(R.drawable.running_shoe_reyda_donmez, R.string.shoe, R.string.shoes, onClickShoe),
      ),
      listOf(
        ImageWithTextData(R.drawable.swimsuit_reyda_donmez, R.string.swimsuit, R.string.one_pieces, onClickSingle),
        ImageWithTextData(R.drawable.sweater_reyda_donmez, R.string.sweater, R.string.outerwear, onClickSweater),
      ),
      listOf(
        ImageWithTextData(R.drawable.skirt_reyda_donmez, R.string.skirt, R.string.bottom, onClickSkirt),
        ImageWithTextData(R.drawable.shirt_reyda_donmez, R.string.shirt, R.string.top, onClickShirt),
      ),
    ).also { ImageWithTextGrid(buttonsTopToBottom = it) }
  }
}