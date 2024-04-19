package com.tambapps.marcel.android.marshell

import android.view.KeyEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onKeyEvent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import com.tambapps.marcel.android.marshell.ui.viewmodel.ShellViewModel

@Composable
fun ShellScreen(viewModel: ShellViewModel = viewModel()) {
  Column(modifier = Modifier.fillMaxSize()) {
    Row(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = "000>",
        style = shellTextStyle
      )

      BasicTextField(value = viewModel.textInput.value,
        onValueChange = { viewModel.textInput.value = it },
        textStyle = shellTextStyle,
        modifier = Modifier.weight(1f)
          .onKeyEvent {
            return@onKeyEvent if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
              // TODO prompt
              viewModel.textInput.value = "youhou"
              true
            } else false
          },
        singleLine = true,
        cursorBrush = SolidColor(Color.White),
      )

    }
  }

}