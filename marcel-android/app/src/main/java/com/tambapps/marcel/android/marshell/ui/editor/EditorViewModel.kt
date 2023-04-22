package com.tambapps.marcel.android.marshell.ui.editor

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor() : ViewModel() {

  val linesCount = MutableLiveData(0)
  val file = MutableLiveData<File?>()

}