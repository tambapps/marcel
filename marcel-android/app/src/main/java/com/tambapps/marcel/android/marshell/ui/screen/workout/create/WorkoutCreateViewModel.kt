package com.tambapps.marcel.android.marshell.ui.screen.workout.create

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriod
import com.tambapps.marcel.android.marshell.ui.screen.ScriptCardEditorViewModel
import com.tambapps.marcel.android.marshell.workout.ShellWorkoutManager
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import marcel.util.Result
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class WorkoutCreateViewModel @Inject constructor(
  private val shellWorkoutManager: ShellWorkoutManager,
  private val shellSessionFactory: ShellSessionFactory
): ViewModel(), ScriptCardEditorViewModel {

  companion object {
    private val VALID_NAME_REGEX = Regex("^[A-Za-z0-9.\\s_-]+\$")
  }

  private val ioScope = CoroutineScope(Dispatchers.IO)
  override var replCompiler = shellSessionFactory.newReplCompiler()
  private var highlighter = SpannableHighlighter(replCompiler)

  override var scriptTextInput by mutableStateOf(TextFieldValue())
  override var scriptTextError by mutableStateOf<String?>(null)
  override val scriptCardExpanded = mutableStateOf(false)

  var name by mutableStateOf("")
  var nameError by mutableStateOf<String?>(null)
  var description by mutableStateOf("")
  var requiresNetwork by mutableStateOf(false)
  var period by mutableStateOf<WorkPeriod?>(null)
  val initScripts = mutableStateListOf<File>()
  // if not null we show the progress dialog
  var progressDialogTitle by mutableStateOf<String?>(null)

  var scheduleAt by mutableStateOf<LocalDateTime?>(null)

  override fun highlight(text: CharSequence) = highlighter.highlight(text)

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
    if (nameError != null && scriptTextError == null) {
      scriptCardExpanded.value = false
    }
    if (nameError != null || scriptTextError != null) {
      return
    }
    CoroutineScope(Dispatchers.IO).launch {
      shellWorkoutManager.save(
        name = name,
        description = description,
        scriptText = scriptTextInput.text,
        period = period,
        scheduleAt = scheduleAt,
        requiresNetwork = requiresNetwork,
        initScripts = initScripts.map { it.absolutePath }.takeIf { it.isNotEmpty() }
      )
      withContext(Dispatchers.Main) {
        onSuccess.invoke()
      }
    }
  }

  fun loadInitScript(context: Context, file: File) {
    progressDialogTitle = "Loading script ${file.name}..."
    ioScope.launch {
      val result = Result.lazy { file.readText() }
        .map(replCompiler::applyAndLoadSemantic)

      withContext(Dispatchers.Main) {
        progressDialogTitle = null
        if (result.isFailure) {
          val e = result.exceptionOrNull
          val message = if (e is MarcelSemanticException || e is MarcelLexerException || e is MarcelParserException)
            "The script doesn't seem to compile. Please fix it before re-importing it."
          else "An error occurred while loading script : ${result.exceptionOrNull?.message}"
          Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } else {
          initScripts.add(file)
        }
      }
    }
  }

  fun unloadInitScript(file: File) {
    if (!initScripts.remove(file)) return
    progressDialogTitle = "Unloading script ${file.name}..."
    resetReplCompiler()
    ioScope.launch {
      for (initScript in initScripts) {
        val initScriptText = file.readText()
        replCompiler.applyAndLoadSemantic(initScriptText)
      }
      withContext(Dispatchers.Main) {
        progressDialogTitle = null
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
    if (shellWorkoutManager.existsByName(name)) {
      nameError = "A work with this name already exists"
      return
    }
    nameError = null
  }

  private fun resetReplCompiler() {
    replCompiler = shellSessionFactory.newReplCompiler()
    highlighter = SpannableHighlighter(replCompiler)
  }
}