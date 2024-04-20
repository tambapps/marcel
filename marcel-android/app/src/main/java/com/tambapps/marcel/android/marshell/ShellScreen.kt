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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tambapps.marcel.android.marshell.repl.ShellSession
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ShellScreen(shellSessionFactory: ShellSessionFactory, scope: CoroutineScope = rememberCoroutineScope(), viewModel: ShellViewModel = viewModel(factory = ShellViewModelFactory(shellSessionFactory))) {
  Column(modifier = Modifier.fillMaxSize()) {
    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), state = listState) {
      item {
        HistoryText(text = viewModel.header.value)
      }
      viewModel.prompts.forEach {  prompt: Prompt ->
        item {
          HistoryText(text = prompt.text, color = when (prompt.type) {
            Prompt.Type.INPUT -> Color.White
            Prompt.Type.SUCCESS_OUTPUT -> Color.Green
            Prompt.Type.ERROR_OUTPUT -> Color.Red
          })
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
fun HistoryText(text: String, color: Color? = null) {
  Text(
    text = text,
    style = color?.let { shellTextStyle.copy(color = it) } ?: shellTextStyle,
  )
}

@Composable
fun PromptButton(viewModel: ShellViewModel, scope: CoroutineScope, listState: LazyListState) {
  IconButton(
    colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.White, disabledContainerColor = Color.Gray),
    enabled = !viewModel.isEvaluating.value,
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

class ShellViewModel constructor(private val shellSession: ShellSession) : ViewModel() {

  // ViewModel logic here
  val textInput = mutableStateOf("")
  val header = mutableStateOf("Marshell (Marcel: 0.1.2, Java: 21.0.2)")
  val prompts = mutableStateListOf<Prompt>()

  val isEvaluating = mutableStateOf(false)

  fun prompt(text: String) {
    prompts.add(Prompt(Prompt.Type.INPUT, text))
    textInput.value = ""
    isEvaluating.value = true
    shellSession.eval(text) { type, result ->
      isEvaluating.value = false
      prompts.add(Prompt(type, java.lang.String.valueOf(result)))
    }
  }
}

class ShellViewModelFactory(
  private val shellSessionFactory: ShellSessionFactory
): ViewModelProvider.Factory {

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return ShellViewModel(shellSessionFactory.newSession()) as T
  }
}

data class Prompt(val type: Type, val text: String) {
  enum class Type {INPUT, SUCCESS_OUTPUT, ERROR_OUTPUT}
}