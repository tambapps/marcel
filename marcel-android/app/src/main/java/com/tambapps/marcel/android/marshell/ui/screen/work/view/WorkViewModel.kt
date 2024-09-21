package com.tambapps.marcel.android.marshell.ui.screen.work.view

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.ui.component.MarkdownComposer
import com.tambapps.marcel.android.marshell.ui.screen.ScriptCardEditorViewModel
import com.tambapps.marcel.android.marshell.work.ShellWorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class WorkViewModel @Inject constructor(
  private val shellWorkManager: ShellWorkManager,
  shellSessionFactory: ShellSessionFactory,
  w: ShellWork?
): ViewModel(), ScriptCardEditorViewModel {

  override val replCompiler = shellSessionFactory.newReplCompiler()
  private val highlighter = SpannableHighlighter(replCompiler)
  val mdComposer = MarkdownComposer(highlighter)

  override var scriptTextInput by mutableStateOf(TextFieldValue())
  override var scriptTextError by mutableStateOf<String?>(null)
  override val scriptCardExpanded = mutableStateOf(false)

  var work by mutableStateOf<ShellWork?>(null)
  var durationBetweenNowAndNext  by mutableStateOf<Duration?>(null) // storing this info in a state to benefit of android compose recomposition
  var scriptEdited by mutableStateOf(false)

  private val ioScope = CoroutineScope(Dispatchers.IO)

  init {
    if (w != null) {
      work = w
      durationBetweenNowAndNext = work?.durationBetweenNowAndNext
      if (w.scriptText != null) {
        setScriptTextInput(w.scriptText)
        scriptEdited = false
      }
    }
  }
  override fun highlight(text: CharSequence) = highlighter.highlight(text)

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