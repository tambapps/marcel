package com.tambapps.marcel.android.marshell.ui.screen.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.ui.screen.ScriptEditorViewModel
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver

class EditorViewModel(
  symbolResolver: ReplMarcelSymbolResolver,
  override val replCompiler: MarcelReplCompiler,
): ViewModel(), ScriptEditorViewModel {

  override var scriptTextInput by mutableStateOf(TextFieldValue())
  override var scriptTextError by mutableStateOf<String?>(null)

  private val highlighter = SpannableHighlighter(symbolResolver, replCompiler)

  override fun highlight(text: CharSequence) = highlighter.highlight(text).toAnnotatedString()

}