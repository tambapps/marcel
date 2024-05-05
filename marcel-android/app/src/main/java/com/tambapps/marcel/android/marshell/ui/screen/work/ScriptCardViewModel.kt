package com.tambapps.marcel.android.marshell.ui.screen.work

import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.input.TextFieldValue
import com.tambapps.marcel.android.marshell.ui.screen.HighlightTransformation

interface ScriptCardViewModel: HighlightTransformation {
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
}