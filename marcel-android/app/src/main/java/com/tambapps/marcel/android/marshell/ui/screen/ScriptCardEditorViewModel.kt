package com.tambapps.marcel.android.marshell.ui.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.input.TextFieldValue
import com.tambapps.marcel.android.marshell.repl.ShellSession
import com.tambapps.marcel.android.marshell.util.readText
import com.tambapps.marcel.repl.MarcelReplCompiler

interface ScriptCardEditorViewModel: ScriptEditorViewModel {
  val scriptCardExpanded: MutableState<Boolean>


  override fun loadScript(context: Context, imageUri: Uri?) {
    super.loadScript(context, imageUri)
    if (imageUri != null) {
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