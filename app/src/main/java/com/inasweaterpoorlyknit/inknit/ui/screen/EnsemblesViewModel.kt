package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.database.repository.EnsembleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Ensemble(
  val id: String,
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
  val ensemblesRepository: EnsembleRepository
): ViewModel() {

  val showAddEnsembleDialog = mutableStateOf(false)

  val ensemblesUiState: StateFlow<List<Ensemble>> =
    ensemblesRepository.getAllEnsembles().map { ensembleEntities ->
      ensembleEntities.map { Ensemble(it.id, it.title, emptyList()) }
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(),
      initialValue = emptyList()
    )

  private fun closeDialog() { showAddEnsembleDialog.value = false }
  fun onClickAddEnsemble() { showAddEnsembleDialog.value = true }
  fun onClickCloseAddEnsembleDialog() = closeDialog()
  fun onClickSaveAddEnsembleDialog(saveEnsembleData: SaveEnsembleData) {
    closeDialog()
    viewModelScope.launch(Dispatchers.IO) {
      ensemblesRepository.insertEnsemble(saveEnsembleData.title)
    }
  }
}