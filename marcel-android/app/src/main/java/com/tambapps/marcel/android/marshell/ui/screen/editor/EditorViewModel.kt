package com.tambapps.marcel.android.marshell.ui.screen.editor

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.ui.screen.ScriptEditorViewModel
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EditorViewModel(
  symbolResolver: ReplMarcelSymbolResolver,
  override val replCompiler: MarcelReplCompiler,
  file: File?
): ViewModel(), ScriptEditorViewModel {

  override var scriptTextInput by mutableStateOf(TextFieldValue())
  override var scriptTextError by mutableStateOf<String?>(null)
  var file by mutableStateOf(file?.let { if (file.isFile || !file.exists()) file else null })

  private val highlighter = SpannableHighlighter(symbolResolver, replCompiler)
  private val ioScope = CoroutineScope(Dispatchers.IO)

  init {
    if (file != null) {
      val result = runCatching { file.readText() }
      if (result.isSuccess) {
        scriptTextInput = TextFieldValue(annotatedString = highlight(result.getOrThrow()))
      }
    }
  }
  override fun highlight(text: CharSequence) = highlighter.highlight(text).toAnnotatedString()

  override fun loadScript(context: Context, file: File?) {
    super.loadScript(context, file)
    if (file != null && file.isFile) {
      this.file = file
    }
  }

  fun validateAndSave(context: Context, file: File): Boolean {
    if (!validateScriptText()) return false
    save(context, file)
    return true
  }

  fun save(context: Context, file: File) {
    ioScope.launch {
      val result = runCatching { file.writeText(scriptTextInput.text) }
      withContext(Dispatchers.Main) {
        Toast.makeText(context,
          if (result.isSuccess) "Saved successfully" else "An error occurred: ${result.exceptionOrNull()?.localizedMessage}"
          , Toast.LENGTH_SHORT).show()
      }
    }
  }
}