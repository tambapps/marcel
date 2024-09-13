package com.tambapps.marcel.android.marshell.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tambapps.marcel.android.marshell.ui.screen.ScriptEditorViewModel
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle

private val LineOfInterestBackgroundColor = Color.LightGray.copy(alpha = 0.4f)

@Composable
fun ScriptTextField(
  viewModel: ScriptEditorViewModel,
  modifier: Modifier = Modifier,
  readOnly: Boolean = false,
  focusRequester: FocusRequester = remember { FocusRequester() }
) {
  val shellTextStyle = MaterialTheme.typography.shellTextStyle
  val style = remember { shellTextStyle.copy(lineHeight = 26.sp) }
  val verticalScrollState = rememberScrollState()

  var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
  val lineCount = textLayoutResult?.lineCount ?: 1
  val lineComponentLength = 12.dp * lineCount.toString().length
  var lineSelected by remember { mutableIntStateOf(0) }

  Row(modifier = modifier) {
    Column(modifier = Modifier
      .fillMaxHeight()
      .verticalScroll(verticalScrollState)) {
      for (i in 0 until lineCount) {
        // using basic text field so that it has the same dimensions as the text text field
        var lineNumberModifier = Modifier.width(lineComponentLength)
        if (lineSelected == i) {
          lineNumberModifier = lineNumberModifier.background(LineOfInterestBackgroundColor)
        }
        BasicTextField(
          modifier = lineNumberModifier,
          value = (i + 1).toString() + " ",
          readOnly = true,
          textStyle = style.copy(textAlign = TextAlign.End),
          onValueChange = {})
      }
    }

    VerticalDivider(
      modifier = Modifier
        .fillMaxHeight()
        .padding(end = 4.dp),
      color = MaterialTheme.colorScheme.onBackground
    )
    BasicTextField(
      modifier = Modifier
        .fillMaxHeight()
        .horizontalScroll(rememberScrollState())
        .wrapContentWidth()
        // this is a hack to prevent this https://stackoverflow.com/questions/76287857/when-parent-of-textfield-is-clickable-hardware-enter-return-button-triggers-its
        .onKeyEvent { it.type == KeyEventType.KeyUp && it.key == Key.Enter }
        .verticalScroll(verticalScrollState)
        .focusRequester(focusRequester),
      value = viewModel.scriptTextInput,
      readOnly = readOnly,
      textStyle = style,
      onTextLayout = { textLayoutResult = it },
      onValueChange = {
        val cursorOffset = it.selection.start
        textLayoutResult?.let { layoutResult ->
          lineSelected = layoutResult.getLineForOffset(cursorOffset)
        }
        viewModel.onScriptTextChange(it)
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
