package com.tambapps.marcel.android.marshell.ui.screen.settings

import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SettingsViewModel: ViewModel() {

  var canManageFiles by mutableStateOf(false)

  init {
    refresh()
  }

  fun refresh() {
    canManageFiles = Environment.isExternalStorageManager()
  }


}