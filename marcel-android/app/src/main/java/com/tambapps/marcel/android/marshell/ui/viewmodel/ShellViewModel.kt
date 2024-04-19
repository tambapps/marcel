package com.tambapps.marcel.android.marshell.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.ui.model.Prompt
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShellViewModel @Inject constructor() : ViewModel() {
    // ViewModel logic here
    val textInput = mutableStateOf("")

    val prompts = mutableStateListOf<Prompt>(Prompt(null, "Marshell (Marcel: 0.1.2, Java: 21.0.2)"))

}
