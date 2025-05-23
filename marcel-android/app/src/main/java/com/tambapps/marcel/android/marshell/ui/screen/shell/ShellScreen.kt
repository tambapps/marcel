package com.tambapps.marcel.android.marshell.ui.screen.shell

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tambapps.marcel.android.marshell.FilePickerActivity
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.Routes
import com.tambapps.marcel.android.marshell.ui.component.TopBarIconButton
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.component.shellIconModifier
import com.tambapps.marcel.android.marshell.ui.theme.disabledPrimary
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import marcel.lang.Markdown
import marcel.util.MarcelVersion

val HEADER = "Marshell (Marcel: ${MarcelVersion.VERSION}, Android ${Build.VERSION.RELEASE})"

@Composable
fun ShellScreen(
  navController: NavController,
  viewModel: ShellViewModel,
  sessionId: Int,
  shellsCount: Int,
  createNewShellClick: () -> Unit
) {
  Column(modifier = Modifier
    .fillMaxSize()
    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
    .imePadding()) {
    TopBar(navController, viewModel, sessionId, shellsCount, createNewShellClick)
    val listState = rememberLazyListState()
    Column(modifier = Modifier
      .weight(1f)) {
      SelectionContainer {
        LazyColumn(
          modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), state = listState
        ) {
          item {
            HistoryText(text = HEADER)
          }
          items(viewModel.prompts) { prompt: Prompt ->
            if (prompt.type == Prompt.Type.INPUT) {
              Row {
                HistoryText(
                  text = "> ",
                  padding = PaddingValues(top = 16.dp)
                )
                if (prompt.text is AnnotatedString) {
                  HistoryText(
                    text = prompt.text, padding = PaddingValues(top = 16.dp)
                  )
                } else {
                  HistoryText(
                    text = prompt.text.toString(), padding = PaddingValues(top = 16.dp)
                  )
                }
              }
            } else if (prompt.value is Markdown) {
              if (viewModel.mdComposer != null) {
                viewModel.mdComposer!!.Markdown(node = prompt.value.node)
              } else {
                HistoryText(text = prompt.text.toString())
              }
            } else {
              HistoryText(
                text = prompt.text.toString(), color = when (prompt.type) {
                  Prompt.Type.INPUT, Prompt.Type.STDOUT -> null
                  Prompt.Type.SUCCESS_OUTPUT -> Color.Green
                  Prompt.Type.ERROR_OUTPUT -> Color.Red
                },
                padding = PaddingValues(top = 8.dp)
              )
            }
          }
        }
      }

    }
    LaunchedEffect(viewModel.prompts.size) {
      // make sure to scroll to the end each time a new item is added on the prompts list
      listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
      val onPrompt: () -> Unit = {
        // when we get the annotatedString here, it is not highlighted yet. So we have to
        val input = viewModel.highlight(viewModel.scriptTextInput.annotatedString)
        if (input.isNotBlank()) {
          viewModel.prompt(input)
        }
      }
      val singleLineInput = viewModel.singleLineInput
      OutlinedTextField(
        value = viewModel.scriptTextInput,
        onValueChange = viewModel::onScriptTextChange,
        visualTransformation = viewModel,
        textStyle = MaterialTheme.typography.shellTextStyle,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(36.dp),
        placeholder = {
          Text(text = viewModel.hint ?: "Type code", color = Color.Gray)
        },
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
private fun TopBar(
  navController: NavController,
  viewModel: ShellViewModel,
  sessionId: Int,
  shellsCount: Int,
  createNewShellClick: () -> Unit,
) {
  val context = LocalContext.current
  TopBarLayout(horizontalArrangement = Arrangement.End) {
   if (shellsCount <= 8) {
     val showConfirmDialog = remember { mutableStateOf(false) }
     ConfirmCreateSessionDialog(
       show = showConfirmDialog,
       createNewShellClick = createNewShellClick
     )
     TopBarIconButton(
       modifier = shellIconModifier(3.dp),
       onClick = { showConfirmDialog.value = true },
       icon = Icons.Filled.Add,
       contentDescription = "New session"
     )
   }

    Box(modifier = Modifier.width(10.dp))

    val clearSessionDialog = remember { mutableStateOf(false) }
    TopBarIconButton(
      modifier = shellIconModifier(3.dp),
      onClick = { clearSessionDialog.value = true },
      drawable = R.drawable.broom,
      contentDescription = "Clear session"
    )
    ClearSessionDialog(
      viewModel = viewModel,
      isOpen = clearSessionDialog.value,
      onDismissRequest = { clearSessionDialog.value = false }
    )


    Box(modifier = Modifier.width(10.dp))

    TopBarIconButton(
      modifier = shellIconModifier(3.dp),
      onClick = { navController.navigate(Routes.consultShell(sessionId)) },
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

    val pickFileLauncher = FilePickerActivity.rememberFilePickerForActivityResult { file ->
      viewModel.loadScript(context, file)
    }
    TopBarIconButton(
      modifier = shellIconModifier(),
      onClick = {
        pickFileLauncher.launch(FilePickerActivity.Args())
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
fun ConfirmCreateSessionDialog(show: MutableState<Boolean>, createNewShellClick: () -> Unit) {
  if (!show.value) return
  AlertDialog(
    title = {
      Text(text = "Create a new shell?")
    },
    text = {
      Text(text = "Do you want to create a new shell?")
    },
    onDismissRequest = { show.value = false },
    dismissButton = {
      TextButton(onClick = { show.value = false }) {
        Text(text = "No")
      }
    },
    confirmButton = {
      TextButton(onClick = {
        createNewShellClick.invoke()
        show.value = false
      }) {
        Text(text = "Yes")
      }
    }
  )
}

@Composable
private fun ClearSessionDialog(
  isOpen: Boolean,
  onDismissRequest: () -> Unit,
  viewModel: ShellViewModel,
) {
  if (!isOpen) {
    return
  }
  val context = LocalContext.current
  AlertDialog(
    title = {
      Text(text = "Clear session")
    },
    text = {
      Text(text = "Clear functions and variables defined in this shell session?")
    },
    onDismissRequest = onDismissRequest,
    dismissButton = {
      TextButton(onClick = onDismissRequest) {
        Text("Cancel")
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          viewModel.clearSession(context)
          onDismissRequest.invoke()
        }
      ) {
        Text("Yes")
      }
    }
  )

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
    viewModel.exportFile(context, uri,
      writeOutput = writeOutput.value, writeStandardOutput = writeStandardOutput.value,
      onlySuccessfulPrompts = onlySuccessfulPrompts.value)
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

@Composable
fun CheckBoxText(valueState: MutableState<Boolean>, text: String, textColor: Color = Color.Unspecified) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { valueState.value = !valueState.value }) {
    Checkbox(checked = valueState.value, onCheckedChange = { isChecked -> valueState.value = isChecked })
    Text(text, color = textColor)
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
    style = color?.let { MaterialTheme.typography.shellTextStyle.copy(color = it) } ?: MaterialTheme.typography.shellTextStyle,
  )
}

@Composable
fun HistoryText(
  text: AnnotatedString,
  color: Color? = null,
  padding: PaddingValues = PaddingValues(all = 0.dp)
) {
  Text(
    modifier = Modifier.padding(padding),
    text = text,
    style = color?.let { MaterialTheme.typography.shellTextStyle.copy(color = it) } ?: MaterialTheme.typography.shellTextStyle,
  )
}

@Composable
fun PromptButton(viewModel: ShellViewModel, onPrompt: () -> Unit) {
  val context = LocalContext.current
  IconButton(
    colors = IconButtonDefaults.iconButtonColors()
      .copy(MaterialTheme.colorScheme.primary, disabledContainerColor = disabledPrimary),
    enabled = !viewModel.isEvaluating && viewModel.isShellReady,
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
    if (viewModel.scriptTextInput.annotatedString.isEmpty()) {
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