package com.tambapps.marcel.android.marshell.ui.screen.works_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tambapps.marcel.android.marshell.work.ShellWorkManager
import javax.inject.Inject

class WorksListViewModelFactory @Inject constructor(
  private val shellWorkManager: ShellWorkManager
): ViewModelProvider.Factory {

  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return WorksListViewModel(shellWorkManager) as T
  }
}
