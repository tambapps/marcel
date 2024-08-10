package com.tambapps.marcel.android.marshell.ui.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.text.input.TextFieldValue
import com.tambapps.marcel.android.marshell.repl.ShellSession
import com.tambapps.marcel.repl.MarcelReplCompiler
import java.io.File

interface ScriptEditorViewModel: ScriptViewModel {
  val replCompiler: MarcelReplCompiler
  var scriptTextError: String?

  fun loadScript(context: Context, file: File?) {
    if (file != null) {
      val result = runCatching { file.readText() }
      if (result.isFailure) {
        Toast.makeText(context, "Error: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_SHORT).show()
        return
      }
      setScriptTextInput(result.getOrNull()!!)
    }
  }

  fun setScriptTextInput(text: CharSequence) {
    scriptTextInput = TextFieldValue(annotatedString = highlight(text))
  }

  fun onScriptTextChange(text: TextFieldValue) {
    if (scriptTextError != null && scriptTextInput.text != text.text) {
      scriptTextError = null
    }
    scriptTextInput = completedInput(text)
  }

  fun validateScriptText(): Boolean {
    val scriptText = scriptTextInput.annotatedString
    if (scriptText.isBlank()) {
      scriptTextError = "Must not be blank"
      return false
    }
    val result = replCompiler.tryParseWithoutUpdateAsResult(scriptText.text)
    if (result.isFailure) {
      val e = result.exceptionOrNull()!!
      scriptTextError = if (ShellSession.isMarcelCompilerException(e)) e.localizedMessage else "An error occurred"
      return false
    }
    scriptTextError = null
    return true
  }

}