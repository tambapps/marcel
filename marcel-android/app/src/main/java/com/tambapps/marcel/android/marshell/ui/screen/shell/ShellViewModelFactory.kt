package com.tambapps.marcel.android.marshell.ui.screen.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import javax.inject.Inject

class ShellViewModelFactory @Inject constructor(
  private val shellSessionFactory: ShellSessionFactory
): ViewModelProvider.Factory {

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return ShellViewModel(shellSessionFactory.newSession()) as T
  }
}
