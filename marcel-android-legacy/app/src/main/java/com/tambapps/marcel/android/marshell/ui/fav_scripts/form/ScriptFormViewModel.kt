package com.tambapps.marcel.android.marshell.ui.fav_scripts.form

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScriptFormViewModel @Inject constructor() : ViewModel() {

  val name = MutableLiveData<String?>()

}