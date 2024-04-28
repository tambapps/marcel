package com.tambapps.marcel.android.marshell.ui.screen.editor

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.ui.screen.HighlightTransformation

class EditorViewModel(
  private val highlighter: SpannableHighlighter
): ViewModel(), HighlightTransformation {

  val textInput = mutableStateOf(TextFieldValue())

  override fun highlight(text: CharSequence) = highlighter.highlight(text).toAnnotatedString()

}