package com.tambapps.marcel.android.marshell.ui.shellwork.form

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class ShellWorkFormViewModel @Inject constructor() : ViewModel() {

  val scriptFile = MutableLiveData<File>()
  val scheduleDate = MutableLiveData<LocalDate?>(null)
  val scheduleTime = MutableLiveData<LocalTime?>(null)
  val period = MutableLiveData<Int?>(null)

}