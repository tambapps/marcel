package com.tambapps.marcel.android.marshell.ui.screen.shell

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import marcel.lang.Script
import java.io.File
import java.io.IOException
import java.io.OutputStream

class ShellViewModel constructor(private val shellSession: ShellSession) : ViewModel(), HighlightTransformation {

  // states
  var textInput by mutableStateOf(TextFieldValue())
  val prompts = mutableStateListOf<Prompt>()
  var isEvaluating by mutableStateOf(false)
  var singleLineInput by mutableStateOf(true)

  // services and miscellaneous
  private val historyNavigator = PromptHistoryNavigator(prompts)
  private val highlighter = shellSession.newHighlighter()
  private val ioScope = CoroutineScope(Dispatchers.IO)

  init {
    shellSession.scriptConfigurer = { script: Script ->
      (script as MarshellScript).setPrinter(PromptPrinter(prompts))
    }
  }

  fun exportFile(
    context: Context, uri: Uri?,
    writeOutput: Boolean,
    writeStandardOutput: Boolean,
    onlySuccessfulPrompts: Boolean
  ) {
    if (uri == null) return
    val typesOfInterest = mutableListOf(Prompt.Type.INPUT)
    if (writeOutput) {
      typesOfInterest.add(Prompt.Type.SUCCESS_OUTPUT)
      typesOfInterest.add(Prompt.Type.ERROR_OUTPUT)
    }
    if (writeStandardOutput) {
      typesOfInterest.add(Prompt.Type.STDOUT)
    }
    val prompts = prompts.filter { typesOfInterest.contains(it.type) }.toMutableList()
    if (onlySuccessfulPrompts) {
      // filter input (and output of input) of error output
      var i = 0
      while (i < prompts.size - 1) {
        val prompt = prompts[i++]
        if (prompt.type != Prompt.Type.INPUT) {
          continue
        }
        val nextOutput = prompts.subList(i, prompts.size).find { it.type == Prompt.Type.SUCCESS_OUTPUT || it.type == Prompt.Type.ERROR_OUTPUT }
        if (nextOutput?.type == Prompt.Type.ERROR_OUTPUT) {
          prompts.remove(prompt)
          prompts.remove(nextOutput)
        }
      }
    }
    // now the export can begin
    val error = export(prompts, context.contentResolver.openOutputStream(uri)).exceptionOrNull()
    if (error != null) {
      Toast.makeText(context, "Error: " + error.localizedMessage, Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "Session exported successfully", Toast.LENGTH_SHORT).show()
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
    textInput = TextFieldValue() // reset text input
    isEvaluating = true
    historyNavigator.reset()
    ioScope.launch {
      val result = shellSession.evalAsResult(text.toString())
      val prompt = if (result.isSuccess) Prompt(Prompt.Type.SUCCESS_OUTPUT, java.lang.String.valueOf(result.getOrNull()))
      else {
        val exception = result.exceptionOrNull()!!
        Log.e("ShellSession", "Error while running prompt", exception)
        Prompt(Prompt.Type.ERROR_OUTPUT, if (ShellSession.isMarcelCompilerException(exception)) exception.localizedMessage else exception.toString())
      }
      withContext(Dispatchers.Main) {
        isEvaluating = false
        prompts.add(prompt)
      }
    }
  }

  override fun highlight(text: CharSequence) = highlighter.highlight(text).toAnnotatedString()

  fun loadScript(context: Context, file: File?) {
    if (file != null) {
      val result = runCatching { file.readText() }
      if (result.isFailure) {
        Toast.makeText(context, "Error: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_SHORT).show()
        return
      }
      setTextInput(result.getOrNull()!!)
    }
  }

  private fun setTextInput(text: CharSequence) {
    textInput = TextFieldValue(annotatedString = highlight(text))
  }

  private fun export(prompts: List<Prompt>, outputStream: OutputStream?): Result<Unit> {
    if (outputStream == null) {
      return Result.failure(IOException("Couldn't open file"))
    }
    try {
      outputStream.bufferedWriter().use { writer ->
        prompts.forEach { prompt ->
          when (prompt.type) {
            Prompt.Type.INPUT -> writer.append(prompt.text)
            Prompt.Type.SUCCESS_OUTPUT -> writer.append("// ${prompt.text}")
            Prompt.Type.ERROR_OUTPUT -> writer.append("// ${prompt.text}")
            Prompt.Type.STDOUT -> writer.append("// STDOUT: ${prompt.text}")
          }
          writer.newLine()
        }
      }
    } catch (e: IOException) {
      return Result.failure(e)
    }
    return Result.success(Unit)
  }
}
