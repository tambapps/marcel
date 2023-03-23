package com.tambapps.marcel.android.app.ui.editor

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.MutableLiveData

class LineCountWatcher(private val linesCount: MutableLiveData<Int>): TextWatcher {
  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
  }

  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    linesCount.value = (s ?: "").count { it == '\n' }
  }

  override fun afterTextChanged(s: Editable?) {

  }
}