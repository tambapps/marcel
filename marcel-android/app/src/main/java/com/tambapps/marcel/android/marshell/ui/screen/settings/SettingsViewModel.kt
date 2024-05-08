package com.tambapps.marcel.android.marshell.ui.screen.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SettingsViewModel: ViewModel() {

  var canManageFiles by mutableStateOf(false)

}