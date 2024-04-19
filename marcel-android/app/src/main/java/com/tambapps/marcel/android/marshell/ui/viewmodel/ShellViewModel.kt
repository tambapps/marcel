package com.tambapps.marcel.android.marshell.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShellViewModel @Inject constructor() : ViewModel() {
    // ViewModel logic here
    val textInput = mutableStateOf("")
}
