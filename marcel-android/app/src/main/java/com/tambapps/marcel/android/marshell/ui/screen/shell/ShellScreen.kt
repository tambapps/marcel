package com.tambapps.marcel.android.marshell.ui.screen.shell

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.ui.component.CheckBoxText
import com.tambapps.marcel.android.marshell.ui.component.TopBarIconButton
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.component.shellIconModifier
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import marcel.lang.util.MarcelVersion
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

val HEADER = "Marshell (Marcel: ${MarcelVersion.VERSION}, Android ${Build.VERSION.RELEASE})"

@Composable
fun ShellScreen(
  viewModel: ShellViewModel,
  scope: CoroutineScope = rememberCoroutineScope(),
) {
  Column(modifier = Modifier.fillMaxSize()) {
    TopBar(viewModel)
    val listState = rememberLazyListState()
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(), state = listState
    ) {
      item {
        HistoryText(text = HEADER)
      }
      viewModel.prompts.forEach { prompt: Prompt ->
        item {
          if (prompt.type == Prompt.Type.INPUT) {
            Row {
              HistoryText(
                text = "> ", color = Color.White,
                padding = PaddingValues(top = 16.dp)
              )
              HistoryText(
                text = prompt.text, color = Color.White,
                padding = PaddingValues(top = 16.dp)
              )
            }
          } else {
            HistoryText(
              text = prompt.text, color = when (prompt.type) {
                Prompt.Type.INPUT, Prompt.Type.STDOUT -> Color.White
                Prompt.Type.SUCCESS_OUTPUT -> Color.Green
                Prompt.Type.ERROR_OUTPUT -> Color.Red
              },
              padding = PaddingValues(top = 8.dp)
            )

          }
        }
      }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      val onPrompt: () -> Unit = {
        val input = viewModel.textInput.text.trim()
        if (input.isNotBlank()) {
          viewModel.prompt(input)
          scope.launch { listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1) }
        }
      }
      val singleLineInput = viewModel.singleLineInput
      OutlinedTextField(
        value = viewModel.textInput,
        onValueChange = { viewModel.textInput = it },
        visualTransformation = viewModel,
        textStyle = shellTextStyle,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(36.dp),
        singleLine = singleLineInput,
        keyboardOptions = if (singleLineInput) KeyboardOptions(imeAction = ImeAction.Done) else KeyboardOptions.Default,
        keyboardActions = if (singleLineInput) KeyboardActions(
          onDone = { onPrompt.invoke() }
        ) else KeyboardActions.Default
      )
      PromptButton(viewModel, onPrompt)
    }
  }
}

@Composable
private fun TopBar(viewModel: ShellViewModel) {
  val context = LocalContext.current
  TopBarLayout(horizontalArrangement = Arrangement.End) {
    TopBarIconButton(
      modifier = shellIconModifier(4.dp),
      onClick = { Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show() },
      drawable = R.drawable.dumbell,
      contentDescription = "handle dumbbells"
    )

    Box(modifier = Modifier.width(10.dp))

    TopBarIconButton(
      modifier = shellIconModifier(3.dp),
      onClick = { Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show() },
      drawable = R.drawable.view,
      contentDescription = "view shell functions/variables"
    )

    Box(modifier = Modifier.width(10.dp))

    val exportDialogOpen = remember { mutableStateOf(false) }
    TopBarIconButton(
      modifier = shellIconModifier(),
      onClick = { exportDialogOpen.value = true },
      drawable = R.drawable.save,
      enabled = viewModel.prompts.any { it.type == Prompt.Type.INPUT },
      contentDescription = "save session to file"
    )
    ExportSessionDialog(
      viewModel = viewModel,
      isOpen = exportDialogOpen.value,
      onDismissRequest = { exportDialogOpen.value = false }
    )

    Box(modifier = Modifier.width(10.dp))

    val pickPictureLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.GetContent()
    ) { imageUri ->
      if (imageUri != null) {
        val result = readText(context.contentResolver.openInputStream(imageUri))
        if (result.isFailure) {
          Toast.makeText(context, "Error: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_SHORT).show()
          return@rememberLauncherForActivityResult
        }
        viewModel.setTextInput(result.getOrNull()!!)
      }
    }
    TopBarIconButton(
      modifier = shellIconModifier(),
      onClick = {
        pickPictureLauncher.launch("*/*")
      },
      drawable = R.drawable.downloads,
      contentDescription = "import script"
    )

    Box(modifier = Modifier.width(10.dp))

    TopBarIconButton(
      modifier = shellIconModifier(),
      onClick = { viewModel.historyUp() },
      drawable = R.drawable.navigate_up_arrow,
      contentDescription = "navigate up"
    )

    Box(modifier = Modifier.width(4.dp))

    TopBarIconButton(
      modifier = shellIconModifier().rotate(180f),
      onClick = { viewModel.historyDown() },
      drawable = R.drawable.navigate_up_arrow,
      contentDescription = "navigate down"
    )
  }
}

@Composable
private fun ExportSessionDialog(
  isOpen: Boolean,
  onDismissRequest: () -> Unit,
  viewModel: ShellViewModel,
) {
  if (!isOpen) {
    return
  }
  val onlySuccessfulPrompts = remember { mutableStateOf(false) }
  val writeOutput = remember { mutableStateOf(false) }
  val writeStandardOutput = remember { mutableStateOf(false) }

  val context = LocalContext.current
  val exportFileLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("*/*")
  ) { uri ->
    if (uri == null) return@rememberLauncherForActivityResult
    val typesOfInterest = mutableListOf(Prompt.Type.INPUT)
    if (writeOutput.value) {
      typesOfInterest.add(Prompt.Type.SUCCESS_OUTPUT)
      typesOfInterest.add(Prompt.Type.ERROR_OUTPUT)
    }
    if (writeStandardOutput.value) {
      typesOfInterest.add(Prompt.Type.STDOUT)
    }
    val prompts = viewModel.prompts.filter { typesOfInterest.contains(it.type) }.toMutableList()
    if (onlySuccessfulPrompts.value) {
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
    onDismissRequest()
  }
  AlertDialog(
    title = {
      Text(text = "Export session to file")
    },
    text = {
      Column {
        CheckBoxText(valueState = onlySuccessfulPrompts, text = "Only successful prompts")
        CheckBoxText(valueState = writeOutput, text = "Include prompt outputs")
        if (viewModel.prompts.any { it.type == Prompt.Type.STDOUT }) {
          CheckBoxText(valueState = writeStandardOutput, text = "Include standard output")
        }
      }
    },
    onDismissRequest = onDismissRequest,
    dismissButton = {
      TextButton(onClick = onDismissRequest) {
        Text("Cancel")
      }
    },
    confirmButton = {
      TextButton(
        onClick = { exportFileLauncher.launch("MarshellSession.mcl") }
      ) {
        Text("Export")
      }
    }
  )
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

fun readText(inputStream: InputStream?): Result<String> {
  if (inputStream == null) {
    return Result.failure(IOException("Couldn't open file"))
  }
  return try {
    Result.success(inputStream.reader().use { it.readText() })
  } catch (e: IOException) {
    Result.failure(e)
  }
}

@Composable
fun HistoryText(
  text: String,
  color: Color? = null,
  padding: PaddingValues = PaddingValues(all = 0.dp)
) {
  Text(
    modifier = Modifier.padding(padding),
    text = text,
    style = color?.let { shellTextStyle.copy(color = it) } ?: shellTextStyle,
  )
}

@Composable
fun PromptButton(viewModel: ShellViewModel, onPrompt: () -> Unit) {
  val context = LocalContext.current
  IconButton(
    colors = IconButtonDefaults.iconButtonColors()
      .copy(containerColor = Color.White, disabledContainerColor = Color.Gray),
    enabled = !viewModel.isEvaluating,
    onClick = OnPromptButtonClick(context, onPrompt, viewModel),
  ) {
    Image(
      painter = painterResource(id = R.drawable.prompt),
      contentDescription = null,
      colorFilter = ColorFilter.tint(Color.Black),
      modifier = Modifier.fillMaxSize(fraction = 0.5f)
    )
  }
}

// when clicking 2 times on an empty textField, it changes the singleLineInput toggle
private class OnPromptButtonClick(
  private val context: Context,
  private val onPrompt: () -> Unit,
  private val viewModel: ShellViewModel
): () -> Unit {

  private var lastClickTimestamp = 0L
  override fun invoke() {
    if (viewModel.textInput.annotatedString.isEmpty()) {
      val now = System.currentTimeMillis()
      if (now - lastClickTimestamp < 500L) {
        viewModel.singleLineInput = !viewModel.singleLineInput
        Toast.makeText(context, "Single line mode: " + (
            if (viewModel.singleLineInput) "ON" else "OFF"
            ), Toast.LENGTH_SHORT).show()
      } else {
        lastClickTimestamp = System.currentTimeMillis()
      }
    } else {
      onPrompt.invoke()
    }
  }
}