package com.tambapps.marcel.android.marshell.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tambapps.marcel.android.marshell.ui.screen.ScriptEditorViewModel
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle

@Composable
fun ScriptTextField(
  viewModel: ScriptEditorViewModel,
  modifier: Modifier = Modifier,
  readOnly: Boolean = false,
  focusRequester: FocusRequester = remember { FocusRequester() }
) {
  var linesText by remember { mutableIntStateOf(1) }
  val shellTextStyle = MaterialTheme.typography.shellTextStyle
  val style = remember { shellTextStyle.copy(lineHeight = 26.sp) }

  val linesTextScroll = rememberScrollState()
  val scriptTextScroll = rememberScrollState()

  // synchronize scrolling
  LaunchedEffect(linesTextScroll.value) {
    scriptTextScroll.scrollTo(linesTextScroll.value)
  }
  LaunchedEffect(scriptTextScroll.value) {
    linesTextScroll.scrollTo(scriptTextScroll.value)
  }

  LaunchedEffect(scriptTextScroll.value) {
    linesTextScroll.scrollTo(scriptTextScroll.value)
  }
  Row(modifier = modifier) {
    BasicTextField(
      modifier = Modifier
        .fillMaxHeight()
        .width(12.dp * linesText.toString().length)
        .verticalScroll(linesTextScroll),
      value = IntRange(1, linesText).joinToString(separator = "\n"),
      readOnly = true,
      textStyle = style.copy(textAlign = TextAlign.End),
      onValueChange = {})

    VerticalDivider(
      modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
      color = MaterialTheme.colorScheme.onBackground
    )
    BasicTextField(
      modifier = Modifier
        .fillMaxHeight()
        .weight(1f)
        // this is a hack to prevent this https://stackoverflow.com/questions/76287857/when-parent-of-textfield-is-clickable-hardware-enter-return-button-triggers-its
        .onKeyEvent { it.type == KeyEventType.KeyUp && it.key == Key.Enter }
        .verticalScroll(scriptTextScroll)
        .focusRequester(focusRequester),
      value = viewModel.scriptTextInput,
      readOnly = readOnly,
      textStyle = style,
      onValueChange = { textFieldValue ->
        val nbLines = textFieldValue.annotatedString.count { it == '\n' } + 1
        if (nbLines != linesText) linesText = nbLines
        viewModel.onScriptTextChange(textFieldValue)
      },
      cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
      visualTransformation = viewModel,
    )
  }
  if (viewModel.scriptTextError != null) {
    Text(
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
      text = viewModel.scriptTextError!!,
      color = MaterialTheme.colorScheme.error
    )
  }
}
