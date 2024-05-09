package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class Ensemble(
  val name: String,
  val thumbnailUriStrings: List<String> = emptyList()
)
data class EnsemblesUiState(
  val ensembles: List<Ensemble>,
  val showAddEnsembleDialog: Boolean,
)
data class SaveEnsembleData(
  val title: String,
)

@HiltViewModel
class EnsemblesViewModel @Inject constructor(
//  val ensemblesRepository: EnsemblesRepository
): ViewModel(){
  val state = mutableStateOf(
    EnsemblesUiState(
      ensembles = emptyList(),
      showAddEnsembleDialog = false,
    )
  )

  private fun closeDialog() {
    state.value = state.value.copy(showAddEnsembleDialog = false)
  }

  fun onClickAddEnsemble() {
    state.value = state.value.copy(showAddEnsembleDialog = true)
  }

  fun onClickCloseAddEnsembleDialog() = closeDialog()

  fun onClickSaveAddEnsembleDialog(saveEnsembleData: SaveEnsembleData) {
    // TODO
    closeDialog()
  }

  fun onClickOutsideAddEnsembleDialog() = closeDialog()
}