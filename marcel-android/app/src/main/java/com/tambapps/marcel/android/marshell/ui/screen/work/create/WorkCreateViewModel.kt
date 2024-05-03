package com.tambapps.marcel.android.marshell.ui.screen.work.create

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.ui.screen.HighlightTransformation

class WorkCreateViewModel(
  private val highlighter: SpannableHighlighter
): ViewModel(), HighlightTransformation {

  val scriptTextInput = mutableStateOf(TextFieldValue())

  override fun highlight(text: CharSequence) = highlighter.highlight(text).toAnnotatedString()

}