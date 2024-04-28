package com.tambapps.marcel.android.marshell.ui.screen.editor

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel

class EditorViewModel: ViewModel() {

  val textInput = mutableStateOf(TextFieldValue())

}