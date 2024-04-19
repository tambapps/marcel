package com.tambapps.marcel.android.marshell

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tambapps.marcel.android.marshell.ui.model.Prompt
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

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

@HiltViewModel
class ShellViewModel @Inject constructor() : ViewModel() {
  // ViewModel logic here
  val textInput = mutableStateOf("")

  val prompts = mutableStateListOf<Prompt>(Prompt(null, "Marshell (Marcel: 0.1.2, Java: 21.0.2)"))

}