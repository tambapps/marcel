package com.tambapps.marcel.android.marshell.ui.screen.shell

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.MarshellScript
import com.tambapps.marcel.android.marshell.repl.ShellSession
import com.tambapps.marcel.android.marshell.repl.console.PromptPrinter
import com.tambapps.marcel.android.marshell.ui.screen.HighlightTransformation
import marcel.lang.Script

class ShellViewModel constructor(private val shellSession: ShellSession) : ViewModel(), HighlightTransformation {

  // states
  var textInput by mutableStateOf(TextFieldValue())
  val prompts = mutableStateListOf<Prompt>()
  var isEvaluating by mutableStateOf(false)
  var singleLineInput by mutableStateOf(true)

  // services and miscellaneous
  private val historyNavigator = PromptHistoryNavigator(prompts)
  private val highlighter = shellSession.newHighlighter()

  init {
    shellSession.scriptConfigurer = { script: Script ->
      (script as MarshellScript).setPrinter(PromptPrinter(prompts))
    }
  }

  fun historyUp() {
    val text = historyNavigator.up() ?: return
    textInput = TextFieldValue(annotatedString = highlighter.highlight(text).toAnnotatedString(), selection = TextRange(text.length))
  }

  fun historyDown() {
    val text = historyNavigator.down() ?: return
    textInput = TextFieldValue(annotatedString = highlighter.highlight(text).toAnnotatedString(), selection = TextRange(text.length))
  }

  fun prompt(text: CharSequence) {
    prompts.add(Prompt(Prompt.Type.INPUT, text))
    textInput = TextFieldValue()
    isEvaluating = true
    // TODO maybe make marcel compiler accept charsequence instead of string, and stop calling toString here
    shellSession.eval(text.toString()) { type, result ->
      isEvaluating = false
      prompts.add(Prompt(type, java.lang.String.valueOf(result)))
    }
    historyNavigator.reset()
  }

  override fun highlight(text: CharSequence) = highlighter.highlight(text).toAnnotatedString()

  fun setTextInput(text: CharSequence) {
    textInput = TextFieldValue(annotatedString = highlight(text))
  }
}
