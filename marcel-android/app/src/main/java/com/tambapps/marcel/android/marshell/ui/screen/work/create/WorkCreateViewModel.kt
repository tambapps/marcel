package com.tambapps.marcel.android.marshell.ui.screen.work.create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.ui.screen.HighlightTransformation

class WorkCreateViewModel(
  private val highlighter: SpannableHighlighter
): ViewModel(), HighlightTransformation {

  companion object {
    private val VALID_NAME_REGEX = Regex("^[A-Za-z0-9.\\s_-]+\$")
  }

  var scriptTextInput by mutableStateOf(TextFieldValue())
  var name by mutableStateOf("")
  var nameError by mutableStateOf<String?>(null)


  var description by mutableStateOf("")
  var requiresNetwork by mutableStateOf(false)
  var silent by mutableStateOf(false)

  override fun highlight(text: CharSequence) = highlighter.highlight(text).toAnnotatedString()

  fun onNameChange(newValue: String) {
    name = newValue
    if (nameError != null) {
      validateName()
    }

  }

  fun setScriptTextInput(text: CharSequence) {
    scriptTextInput = TextFieldValue(annotatedString = highlight(text))
  }

  fun validateAndSave() {
    validateName()

  }

  private fun validateName() {
    if (name.isBlank()) {
      nameError = "Must not be empty"
      return
    }
    if (!VALID_NAME_REGEX.matches(name)) {
      nameError = "Must not contain illegal character"
      return
    }
    if (name.length > 100) {
      nameError = "Must not be longer than 100 chars"
    }
    nameError = null
  }
}