package com.tambapps.marcel.android.marshell.ui.screen.work.view

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.ui.screen.ScriptCardEditorViewModel
import com.tambapps.marcel.android.marshell.work.ShellWorkManager
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration

class WorkViewModel(
  private val shellWorkManager: ShellWorkManager,
  symbolResolver: ReplMarcelSymbolResolver,
  override val replCompiler: MarcelReplCompiler,
  workName: String?
): ViewModel(), ScriptCardEditorViewModel {

  override var scriptTextInput by mutableStateOf(TextFieldValue())
  override var scriptTextError by mutableStateOf<String?>(null)
  override val scriptCardExpanded = mutableStateOf(false)

  var work by mutableStateOf<ShellWork?>(null)
  var durationBetweenNowAndNext  by mutableStateOf<Duration?>(null) // storing this info in a state to benefit of android compose recomposition
  var scriptEdited by mutableStateOf(false)

  private val highlighter = SpannableHighlighter(symbolResolver, replCompiler)
  private val ioScope = CoroutineScope(Dispatchers.IO)

  init {
    if (workName != null) {
      ioScope.launch {
        val w = shellWorkManager.findByName(workName)
        if (w != null) {
          withContext(Dispatchers.Main) {
            work = w
            durationBetweenNowAndNext = work?.durationBetweenNowAndNext
            if (w.scriptText != null) {
              setScriptTextInput(w.scriptText)
              scriptEdited = false
            }
          }
        }
      }
    }
  }
  override fun highlight(text: CharSequence) = highlighter.highlight(text).toAnnotatedString()

  override fun onScriptTextChange(text: TextFieldValue) {
    super.onScriptTextChange(text)
    if (work?.isPeriodic == true) {
      scriptEdited = true
    }
  }

  suspend fun refresh(workName: String) {
    val w = shellWorkManager.findByName(workName)
    withContext(Dispatchers.Main) {
      work = w
      durationBetweenNowAndNext = work?.durationBetweenNowAndNext
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

  fun cancelWork(context: Context, workName: String) {
    ioScope.launch {
      val updatedWork = shellWorkManager.cancel(workName)
      withContext(Dispatchers.Main) {
        work = updatedWork
        scriptEdited = false
        Toast.makeText(context, "Work successfully cancelled", Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun deleteWork(context: Context, workName: String, navController: NavController) {
    ioScope.launch {
      shellWorkManager.delete(workName)
      withContext(Dispatchers.Main) {
        navController.navigateUp()
        Toast.makeText(context, "Work successfully deleted", Toast.LENGTH_SHORT).show()
      }
    }
  }
}