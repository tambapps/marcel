package com.tambapps.marcel.android.marshell

import android.view.KeyEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tambapps.marcel.android.marshell.ui.model.Prompt
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import com.tambapps.marcel.android.marshell.ui.viewmodel.ShellViewModel

@Composable
fun ShellScreen(viewModel: ShellViewModel = viewModel()) {
  Column(modifier = Modifier.fillMaxSize()) {
    LazyColumn {
      viewModel.prompts.forEach {  prompt: Prompt ->
        if (prompt.input != null) {
          item {
            Text(
              text = prompt.input,
              style = shellTextStyle
            )
          }
        }

        if (prompt.output != null) {
          item {
            Text(
              text = prompt.output,
              style = shellTextStyle
            )
          }
        }
      }
    }
    OutlinedTextField(value = viewModel.textInput.value,
      onValueChange = { viewModel.textInput.value = it },
      textStyle = shellTextStyle,
      keyboardActions = KeyboardActions(onDone = {
        viewModel.textInput.value = "youhou"
      }),
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(36.dp)
    )
  }
}