package com.tambapps.marcel.android.marshell.ui.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.text.input.TextFieldValue
import com.tambapps.marcel.android.marshell.repl.ShellSession
import com.tambapps.marcel.android.marshell.util.readText
import com.tambapps.marcel.repl.MarcelReplCompiler

interface ScriptEditorViewModel: HighlightTransformation {
  val replCompiler: MarcelReplCompiler
  var scriptTextInput: TextFieldValue
  var scriptTextError: String?

  fun loadScript(context: Context, imageUri: Uri?) {
    if (imageUri != null) {
      val result = readText(context.contentResolver.openInputStream(imageUri))
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
    scriptTextInput = text
    if (scriptTextError != null) {
      scriptTextError = null
    }
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