package com.tambapps.marcel.android.marshell.ui.screen.shell

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import marcel.lang.Script

class ShellViewModel constructor(private val shellSession: ShellSession) : ViewModel() {

  // ViewModel logic here
  val textInput = mutableStateOf(TextFieldValue())
  val prompts = mutableStateListOf<Prompt>()
  val isEvaluating = mutableStateOf(false)
  private val highlighter = shellSession.newHighlighter()
  private var historyIndex = -1
  private val highlightScope = CoroutineScope(Dispatchers.IO)
  private var job: Job? = null

  init {
    shellSession.scriptConfigurer = { script: Script ->
      (script as MarshellScript).setPrinter(PromptPrinter(prompts))
    }
  }

  // TODO fix history functions
  fun historyUp() {
    val prompts = this.prompts.filter { it.type == Prompt.Type.INPUT }
    if (prompts.isEmpty() || prompts.size <= historyIndex) return
    val text = prompts[prompts.size - 1 - ++historyIndex].text
    textInput.value = textInput.value.copy(annotatedString = highlighter.highlight(text).toAnnotatedString())
  }

  fun historyDown() {
    val prompts = this.prompts.filter { it.type == Prompt.Type.INPUT }
    if (prompts.isEmpty() || historyIndex <= 0) return
    val text = prompts[prompts.size - 1 - --historyIndex].text
    textInput.value = textInput.value.copy(annotatedString = highlighter.highlight(text).toAnnotatedString())
  }

  fun prompt(text: String) {
    prompts.add(Prompt(Prompt.Type.INPUT, text))
    textInput.value = TextFieldValue()
    isEvaluating.value = true
    shellSession.eval(text) { type, result ->
      isEvaluating.value = false
      prompts.add(Prompt(type, java.lang.String.valueOf(result)))
    }
    historyIndex = -1
  }

  fun highlightTextInput() {
    job?.cancel()
    job = highlightScope.launch {
      delay(500)
      textInput.value = textInput.value.copy(annotatedString = highlighter.highlight(textInput.value.annotatedString).toAnnotatedString())
    }
  }
}
