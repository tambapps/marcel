package com.tambapps.marcel.android.marshell.ui.component

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.repl.MarshellScript
import com.tambapps.marcel.android.marshell.repl.ShellSession
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.repl.console.PromptPrinter
import com.tambapps.marcel.android.marshell.ui.theme.TopBarIconSize
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import marcel.lang.Script
import marcel.lang.util.MarcelVersion

val HEADER = "Marshell (Marcel: ${MarcelVersion.VERSION}, Android ${Build.VERSION.RELEASE})"

@Composable
fun ShellScreen(shellSessionFactory: ShellSessionFactory, scope: CoroutineScope = rememberCoroutineScope(), viewModel: ShellViewModel = viewModel(factory = ShellViewModelFactory(shellSessionFactory))) {
  Column(modifier = Modifier.fillMaxSize()) {
    TopBar()
    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier
      .weight(1f)
      .fillMaxWidth(), state = listState) {
      item {
        HistoryText(text = HEADER)
      }
      viewModel.prompts.forEach { prompt: Prompt ->
        item {
          if (prompt.type == Prompt.Type.INPUT) {
            Row {
              HistoryText(text = "> ", color = Color.White,
                padding = PaddingValues(top = 16.dp)
              )
              HistoryText(text = prompt.text, color = Color.White,
                padding = PaddingValues(top = 16.dp)
              )
            }
          } else {
            HistoryText(text = prompt.text, color = when (prompt.type) {
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
      OutlinedTextField(value = viewModel.textInput.value,
        onValueChange = { viewModel.textInput.value = it.copy(annotatedString = viewModel.highlighter.highlight(it.annotatedString).toAnnotatedString()) },
        textStyle = shellTextStyle,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(36.dp)
      )
      PromptButton(viewModel, scope, listState)
    }
  }
}

@Composable
fun TopBar() {
  TopBarLayout(horizontalArrangement = Arrangement.End) {
    IconButton(
      imageVector = Icons.Filled.KeyboardArrowUp,
      modifier = Modifier.padding(end = 8.dp),
      size = TopBarIconSize,
      onClick = {

      }
    )
    IconButton(
      imageVector = Icons.Filled.KeyboardArrowDown,
      size = TopBarIconSize,
      onClick = {

      }
    )


  }
}
@Composable
fun HistoryText(text: String, color: Color? = null, padding: PaddingValues = PaddingValues(all = 0.dp)) {
  Text(
    modifier = Modifier.padding(padding),
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
      val input = viewModel.textInput.value.text.trim()
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
  val textInput = mutableStateOf(TextFieldValue())
  val prompts = mutableStateListOf<Prompt>()
  val isEvaluating = mutableStateOf(false)
  val highlighter = shellSession.newHighlighter()

  init {
    shellSession.scriptConfigurer = { script: Script ->
      (script as MarshellScript).setPrinter(PromptPrinter(prompts))
    }
  }
  fun prompt(text: String) {
    prompts.add(Prompt(Prompt.Type.INPUT, text))
    textInput.value = TextFieldValue()
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
  enum class Type {INPUT, SUCCESS_OUTPUT, ERROR_OUTPUT, STDOUT}
}