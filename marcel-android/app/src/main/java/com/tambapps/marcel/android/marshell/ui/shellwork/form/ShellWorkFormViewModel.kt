package com.tambapps.marcel.android.marshell.ui.shellwork.form

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ShellWorkFormViewModel @Inject constructor() : ViewModel() {

  val scriptFile = MutableLiveData<File>()

}