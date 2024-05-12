package com.tambapps.marcel.android.marshell.ui.screen.settings

import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SettingsViewModel @Inject constructor(
  @Named("initScriptFile")
  val initScriptFile: File
  ): ViewModel() {

  var canManageFiles by mutableStateOf(false)

  init {
    refresh()
  }

  fun refresh() {
    canManageFiles = Environment.isExternalStorageManager()
  }


}