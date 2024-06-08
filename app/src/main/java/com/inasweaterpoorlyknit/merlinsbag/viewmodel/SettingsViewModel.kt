package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.data.repository.PurgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val purgeRepository: PurgeRepository,
): ViewModel() {
  private val _cacheClearedTrigger = MutableSharedFlow<Unit>()
  val cacheClearedTrigger: SharedFlow<Unit> = _cacheClearedTrigger

  private val _allDataDeletedTrigger = MutableSharedFlow<Unit>()
  val allDataDeletedTrigger: SharedFlow<Unit> = _allDataDeletedTrigger

  fun clearCache() = viewModelScope.launch(Dispatchers.IO) {
    purgeRepository.purgeCache()
    _cacheClearedTrigger.emit(Unit)
  }
  fun deleteAllData() = viewModelScope.launch(Dispatchers.IO) {
    purgeRepository.purgeUserData()
    _allDataDeletedTrigger.emit(Unit)
  }
}