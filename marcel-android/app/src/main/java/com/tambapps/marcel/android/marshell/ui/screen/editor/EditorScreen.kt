package com.tambapps.marcel.android.marshell.ui.screen.editor

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tambapps.marcel.android.marshell.FilePickerActivity
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.ui.component.ScriptTextField
import com.tambapps.marcel.android.marshell.ui.component.TopBarIconButton
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.component.shellIconModifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun EditorScreen(viewModel: EditorViewModel) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope { Dispatchers.IO }
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
      FloatingActionButton(
        modifier = Modifier.padding(all = 16.dp),
        onClick = {
          val file = viewModel.file
          if (file != null) {
            scope.launch {
              val result = runCatching { file.writeText(viewModel.scriptTextInput.text) }
              withContext(Dispatchers.Main) {
                Toast.makeText(context,
                  if (result.isSuccess) "Saved successfully" else "An error occurred: ${result.exceptionOrNull()?.localizedMessage}"
                  , Toast.LENGTH_SHORT).show()
              }
            }
          } else {
            Toast.makeText(context, "todo", Toast.LENGTH_SHORT).show()
            /*TODO pick file and then save on it. also allow creating new file in filePicker*/
          }
        }
      ) {
        Icon(
          modifier = Modifier.size(23.dp),
          painter = painterResource(id = R.drawable.save),
          contentDescription = "Save",
          tint = Color.White
        )
      }
    }
  }

}
@Composable
private fun TopBar(viewModel: EditorViewModel) {
  val context = LocalContext.current
  val pickFileLauncher = FilePickerActivity.rememberFilePickerForActivityResult { file ->
    viewModel.loadScript(context, file)
  }
  TopBarLayout(horizontalArrangement = Arrangement.End) {
    TopBarIconButton(
      modifier = shellIconModifier(4.dp),
      onClick = { pickFileLauncher.launch(FilePickerActivity.Args(allowCreateNewFile = true)) },
      drawable = R.drawable.folder,
      contentDescription = "Open file"
    )

  }
}