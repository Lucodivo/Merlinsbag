package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class Collection(
  val name: String,
  val thumbnailUriStrings: List<String> = emptyList()
)
data class CollectionsUiState(
  val collections: List<Collection>,
  val showAddCollectionDialog: Boolean,
)

@HiltViewModel
class CollectionsViewModel @Inject constructor(
//  val collectionsRepository: CollectionsRepository
): ViewModel(){
  val state = mutableStateOf(
    CollectionsUiState(
      collections = emptyList(),
      showAddCollectionDialog = false,
    )
  )

  private fun closeDialog() {
    state.value = state.value.copy(showAddCollectionDialog = false)
  }

  fun onClickAddCollection() {
    state.value = state.value.copy(showAddCollectionDialog = true)
  }

  fun onClickCloseAddCollectionDialog() = closeDialog()

  fun onClickSaveAddCollectionDialog() {
    // TODO
    closeDialog()
  }

  fun onClickOutsideAddCollectionDialog() = closeDialog()
}