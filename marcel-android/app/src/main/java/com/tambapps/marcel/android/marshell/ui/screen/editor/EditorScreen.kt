package com.tambapps.marcel.android.marshell.ui.screen.editor

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tambapps.marcel.android.marshell.FilePickerActivity
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.ui.component.ScriptTextField
import com.tambapps.marcel.android.marshell.ui.component.TopBarIconButton
import com.tambapps.marcel.android.marshell.ui.component.shellIconModifier
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight

@Composable
fun EditorScreen(viewModel: EditorViewModel) {
  val context = LocalContext.current
  Column(modifier = Modifier.fillMaxSize()) {
    TopBar(viewModel)
    Box(modifier = Modifier
      .weight(1f)
      .fillMaxWidth(),
      contentAlignment = Alignment.BottomEnd) {

      ScriptTextField(
        viewModel = viewModel,
        modifier = Modifier.fillMaxSize(),
      )
      val pickFileLauncher = FilePickerActivity.rememberFilePickerForActivityResult { file ->
        if (file != null) {
          viewModel.save(context, file)
          viewModel.file = file
        }
      }
      val showErrorDialog = remember { mutableStateOf(false) }
      WarningDialog(
        showState = showErrorDialog,
        error = viewModel.scriptTextError,
        onConfirm = {
          if (viewModel.file != null) {
            viewModel.save(context, viewModel.file!!)
            viewModel.file = viewModel.file!!
          }
          else {
            Toast.makeText(context, "Please select a file to save", Toast.LENGTH_SHORT).show()
            pickFileLauncher.launch(FilePickerActivity.Args(allowCreateNewFile = true))
          }
        }
      )
      FloatingActionButton(
        modifier = Modifier.padding(all = 16.dp),
        onClick = {
          val file = viewModel.file
          if (file != null) {
            viewModel.file = file
            if (!viewModel.validateAndSave(context, file)) {
              showErrorDialog.value = true
            }
          } else {
            if (viewModel.validateScriptText()) {
              Toast.makeText(context, "Please select a file to save", Toast.LENGTH_SHORT).show()
              pickFileLauncher.launch(FilePickerActivity.Args(allowCreateNewFile = true))
            } else {
              showErrorDialog.value = true
            }
          }
        }
      ) {
        Icon(
          modifier = Modifier.size(23.dp),
          painter = painterResource(id = R.drawable.save),
          contentDescription = "Save",
        )
      }
    }
  }
}

@Composable
fun WarningDialog(
  showState: MutableState<Boolean>,
  error: String?,
  onConfirm: () -> Unit
) {
  if (!showState.value) return
  AlertDialog(
    title = {
       Text(text = "Compilation error")
    },
    text = {
      Text(text =
      (error?.let { "$it\n\n" } ?: "") +
      "Your code doesn't compile. Do you want to save anyway?")
    },
    onDismissRequest = { showState.value = false },
    confirmButton = {
      TextButton(onClick = { onConfirm.invoke(); showState.value = false }) {
        Text(text = "Yes")
      }
    },
    dismissButton = {
      TextButton(onClick = { showState.value = false }) {
        Text(text = "No")
      }
    }
  )

}

@Composable
private fun TopBar(viewModel: EditorViewModel) {
  val context = LocalContext.current
  val pickFileLauncher = FilePickerActivity.rememberFilePickerForActivityResult { file ->
    viewModel.loadScript(context, file)
  }
  Box(modifier = Modifier
    .fillMaxWidth()
    .height(TopBarHeight)) {
    TopBarIconButton(
      modifier = shellIconModifier(4.dp).align(Alignment.CenterEnd),
      onClick = { pickFileLauncher.launch(FilePickerActivity.Args(allowCreateNewFile = true)) },
      drawable = R.drawable.folder,
      contentDescription = "Open file"
    )

    if (viewModel.file != null) {
      Text(text = viewModel.file!!.name, fontSize = 20.sp, modifier = Modifier.align(Alignment.Center), color = Color.White)
    }
  }
}