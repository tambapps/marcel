package com.tambapps.marcel.android.marshell.ui.screen.work.create

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriod
import com.tambapps.marcel.android.marshell.service.PreferencesDataStore
import com.tambapps.marcel.android.marshell.ui.screen.ScriptCardEditorViewModel
import com.tambapps.marcel.android.marshell.work.ShellWorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class WorkCreateViewModel @Inject constructor(
  private val shellWorkManager: ShellWorkManager,
  val preferencesDataStore: PreferencesDataStore,
  private val notificationManager: NotificationManager,
  shellSessionFactory: ShellSessionFactory
): ViewModel(), ScriptCardEditorViewModel {

  companion object {
    private val VALID_NAME_REGEX = Regex("^[A-Za-z0-9.\\s_-]+\$")
  }

  override val replCompiler = shellSessionFactory.newReplCompiler()
  private val highlighter = SpannableHighlighter(replCompiler)

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

  val shouldNotificationsPermission
    get() = !silent // only ask permission if non silent
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU // asking permission is only required since Android TIRAMISU
        && !notificationManager.areNotificationsEnabled() // only ask if it isn't enabled
}