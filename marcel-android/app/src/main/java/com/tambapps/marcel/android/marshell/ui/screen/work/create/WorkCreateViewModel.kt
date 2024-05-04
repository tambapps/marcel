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

  var scriptTextInput by mutableStateOf(TextFieldValue())
  var name by mutableStateOf("")
  var description by mutableStateOf("")
  var requiresNetwork by mutableStateOf(false)
  var silent by mutableStateOf(false)

  override fun highlight(text: CharSequence) = highlighter.highlight(text).toAnnotatedString()

}