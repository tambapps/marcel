package com.tambapps.marcel.android.marshell.ui.screen

import android.content.Context
import androidx.compose.runtime.MutableState
import java.io.File

interface ScriptCardEditorViewModel: ScriptEditorViewModel {
  val scriptCardExpanded: MutableState<Boolean>

  override fun loadScript(context: Context, file: File?) {
    super.loadScript(context, file)
    if (file != null) {
      scriptCardExpanded.value = true
    }
  }

  override fun validateScriptText(): Boolean {
    return if (super.validateScriptText()) true
    else {
      scriptCardExpanded.value = true
      false
    }
  }

}