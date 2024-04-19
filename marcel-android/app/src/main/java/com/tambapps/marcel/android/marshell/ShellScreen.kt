package com.tambapps.marcel.android.marshell

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tambapps.marcel.android.marshell.ui.model.Prompt
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun ShellScreen(scope: CoroutineScope = rememberCoroutineScope(), viewModel: ShellViewModel = viewModel()) {
  Column(modifier = Modifier.fillMaxSize()) {
    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), state = listState) {
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

    Row {
      OutlinedTextField(value = viewModel.textInput.value,
        onValueChange = { viewModel.textInput.value = it },
        textStyle = shellTextStyle,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(36.dp)
      )
      PromptButton(viewModel, scope, listState)
    }
  }
}

@Composable
fun PromptButton(viewModel: ShellViewModel, scope: CoroutineScope, listState: LazyListState) {
  IconButton(
    colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.White, disabledContainerColor = Color.Gray),
    enabled = !viewModel.isProcessing,
    onClick = {
      val input = viewModel.textInput.value
      if (input.isNotBlank()) {
        viewModel.prompt(input)
        scope.launch { listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1) }
      }
    },
  ) {
    Image(
      painter = painterResource(id = R.drawable.prompt),
      contentDescription = null,
      colorFilter = ColorFilter.tint(Color.Black),
      modifier = Modifier.fillMaxSize(fraction = 0.5f)
    )
  }
}

@HiltViewModel
class ShellViewModel @Inject constructor() : ViewModel() {
  // ViewModel logic here
  val textInput = mutableStateOf("")

  val prompts = mutableStateListOf<Prompt>(Prompt(null, "Marshell (Marcel: 0.1.2, Java: 21.0.2)"))

  val isProcessing get() = prompts.lastOrNull()?.let { it.output == null } ?: false

  fun prompt(input: String) {
    prompts.add(Prompt(input, null))
    textInput.value = ""
  }
}