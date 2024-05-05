package com.tambapps.marcel.android.marshell.ui.screen.work.view

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.ui.screen.HighlightTransformation
import com.tambapps.marcel.android.marshell.ui.screen.work.ScriptCardViewModel
import com.tambapps.marcel.android.marshell.work.ShellWorkManager
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WorkViewModel(
  private val shellWorkManager: ShellWorkManager,
  symbolResolver: ReplMarcelSymbolResolver,
  override val replCompiler: MarcelReplCompiler,
): ViewModel(), ScriptCardViewModel {

  override var scriptTextInput by mutableStateOf(TextFieldValue())
  override var scriptTextError by mutableStateOf<String?>(null)
  override val scriptCardExpanded = mutableStateOf(false)

  var work by mutableStateOf<ShellWork?>(null)
  var scriptEdited by mutableStateOf(false)

  private val highlighter = SpannableHighlighter(symbolResolver, replCompiler)
  private val ioScope = CoroutineScope(Dispatchers.IO)

  override fun highlight(text: CharSequence) = highlighter.highlight(text).toAnnotatedString()

  override fun onScriptTextChange(text: TextFieldValue) {
    super.onScriptTextChange(text)
    scriptEdited = true
  }

  fun init(workName: String) {
    ioScope.launch {
      val w = shellWorkManager.findByName(workName)
      if (w != null) {
        withContext(Dispatchers.Main) {
          work = w
          if (w.scriptText != null) {
            setScriptTextInput(w.scriptText)
            scriptEdited = false
          }
        }
      }
    }
  }

  fun validateAndSave(context: Context) {
    val workName = this.work?.name ?: return
    validateScriptText()
    if (scriptTextError != null) {
      return
    }
    ioScope.launch {
      val updatedWork = shellWorkManager.update(workName, scriptTextInput.text)
      withContext(Dispatchers.Main) {
        work = updatedWork
        scriptEdited = false
        Toast.makeText(context, "Work successfully updated", Toast.LENGTH_SHORT).show()
      }
    }
  }
}