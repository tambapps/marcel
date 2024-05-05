package com.tambapps.marcel.android.marshell.ui.screen.work.list

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.work.ShellWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorksListViewModel(
  private val shellWorkManager: ShellWorkManager
): ViewModel() {

  init {
    CoroutineScope(Dispatchers.IO).launch {
      val list = shellWorkManager.listFake()
      withContext(Dispatchers.Main) {
        works.addAll(list)
      }
    }
  }

  val works = mutableStateListOf<ShellWork>()
}