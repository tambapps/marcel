package com.tambapps.marcel.android.marshell.ui.screen.work

import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.input.TextFieldValue
import com.tambapps.marcel.android.marshell.repl.ShellSession
import com.tambapps.marcel.android.marshell.ui.screen.HighlightTransformation
import com.tambapps.marcel.repl.MarcelReplCompiler

interface ScriptCardViewModel: HighlightTransformation {
  val replCompiler: MarcelReplCompiler
  val scriptCardExpanded: MutableState<Boolean>
  var scriptTextInput: TextFieldValue
  var scriptTextError: String?

  fun setScriptTextInput(text: CharSequence) {
    scriptTextInput = TextFieldValue(annotatedString = highlight(text))
  }

  fun onScriptTextChange(text: TextFieldValue) {
    scriptTextInput = text
    if (scriptTextError != null) {
      scriptTextError = null
    }
  }

  fun validateScriptText() {
    val scriptText = scriptTextInput.annotatedString
    if (scriptText.isBlank()) {
      scriptTextError = "Must not be blank"
      scriptCardExpanded.value = true
      return
    }
    val result = replCompiler.tryParseWithoutUpdateAsResult(scriptText.text)
    if (result.isFailure) {
      val e = result.exceptionOrNull()!!
      scriptTextError = if (ShellSession.isMarcelCompilerException(e)) e.localizedMessage else "An error occurred"
      scriptCardExpanded.value = true
      return
    }
    scriptTextError = null
  }

}