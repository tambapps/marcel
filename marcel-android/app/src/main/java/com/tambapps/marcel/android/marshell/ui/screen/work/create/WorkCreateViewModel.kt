package com.tambapps.marcel.android.marshell.ui.screen.work.create

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.ShellSession
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriod
import com.tambapps.marcel.android.marshell.ui.screen.HighlightTransformation
import com.tambapps.marcel.android.marshell.ui.screen.work.ScriptCardViewModel
import com.tambapps.marcel.android.marshell.work.ShellWorkManager
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class WorkCreateViewModel(
  private val shellWorkManager: ShellWorkManager,
  symbolResolver: ReplMarcelSymbolResolver,
  override val replCompiler: MarcelReplCompiler,
): ViewModel(), ScriptCardViewModel {

  companion object {
    private val VALID_NAME_REGEX = Regex("^[A-Za-z0-9.\\s_-]+\$")
  }

  override var scriptTextInput by mutableStateOf(TextFieldValue())
  override var scriptTextError by mutableStateOf<String?>(null)
  override val scriptCardExpanded = mutableStateOf(false)

  var name by mutableStateOf("")
  var nameError by mutableStateOf<String?>(null)
  var description by mutableStateOf("")
  var requiresNetwork by mutableStateOf(false)
  var silent by mutableStateOf(false)
  var period by mutableStateOf<WorkPeriod?>(null)

  var scheduleAt by mutableStateOf<LocalDateTime?>(null)

  private val highlighter = SpannableHighlighter(symbolResolver, replCompiler)

  override fun highlight(text: CharSequence) = highlighter.highlight(text).toAnnotatedString()

  fun onNameChange(newValue: String) {
    name = newValue
    if (nameError != null) {
      validateName()
    }
  }

  fun validateAndSave(context: Context, onSuccess: () -> Unit) {
    validateName()
    validateScriptText()
    if (scheduleAt?.isBefore(LocalDateTime.now().plusMinutes(1)) == true) {
      Toast.makeText(context, "Time must be at least 15 minutes from now", Toast.LENGTH_SHORT).show()
      return
    }
    if (nameError != null || scriptTextError != null) {
      return
    }
    CoroutineScope(Dispatchers.IO).launch {
      shellWorkManager.save(
        name = name,
        description = description,
        scriptText = scriptTextInput.text,
        period = period,
        scheduleAt = scheduleAt,
        requiresNetwork = requiresNetwork,
        silent = silent
      )
      withContext(Dispatchers.Main) {
        onSuccess.invoke()
      }
    }
  }

  private fun validateName() {
    if (name.isBlank()) {
      nameError = "Must not be blank"
      return
    }
    if (!VALID_NAME_REGEX.matches(name)) {
      nameError = "Must not contain illegal character"
      return
    }
    if (name.length > 100) {
      nameError = "Must not be longer than 100 chars"
      return
    }
    if (shellWorkManager.existsByName(name)) {
      nameError = "A work with this name already exists"
      return
    }
    nameError = null
  }
}