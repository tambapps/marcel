package com.tambapps.marcel.android.marshell.ui.screen.shell

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.MarshellScript
import com.tambapps.marcel.android.marshell.repl.ShellSession
import com.tambapps.marcel.android.marshell.repl.console.PromptPrinter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import marcel.lang.Script

class ShellViewModel constructor(private val shellSession: ShellSession) : ViewModel() {

  // states
  val textInput = mutableStateOf(TextFieldValue())
  val prompts = mutableStateListOf<Prompt>()
  val isEvaluating = mutableStateOf(false)

  // services and miscellaneous
  private val historyNavigator = PromptHistoryNavigator(prompts)
  private val highlighter = shellSession.newHighlighter()
  private val highlightScope = CoroutineScope(Dispatchers.IO)
  private var job: Job? = null

  init {
    shellSession.scriptConfigurer = { script: Script ->
      (script as MarshellScript).setPrinter(PromptPrinter(prompts))
    }
  }

  fun historyUp() {
    val text = historyNavigator.up() ?: return
    textInput.value = TextFieldValue(annotatedString = highlighter.highlight(text).toAnnotatedString(), selection = TextRange(text.length))
  }

  fun historyDown() {
    val text = historyNavigator.down() ?: return
    textInput.value = TextFieldValue(annotatedString = highlighter.highlight(text).toAnnotatedString(), selection = TextRange(text.length))
  }

  fun prompt(text: String) {
    prompts.add(Prompt(Prompt.Type.INPUT, text))
    textInput.value = TextFieldValue()
    isEvaluating.value = true
    shellSession.eval(text) { type, result ->
      isEvaluating.value = false
      prompts.add(Prompt(type, java.lang.String.valueOf(result)))
    }
    historyNavigator.reset()
  }

  fun highlightTextInput(text: String) {
    textInput.value = TextFieldValue(text = text)
    highlightScope.launch {
      textInput.value = textInput.value.copy(annotatedString = highlighter.highlight(textInput.value.annotatedString).toAnnotatedString())
    }
  }

  fun highlightTextInput() {
    job?.cancel()
    job = highlightScope.launch {
      delay(500)
      textInput.value = textInput.value.copy(annotatedString = highlighter.highlight(textInput.value.annotatedString).toAnnotatedString())
    }
  }
}
