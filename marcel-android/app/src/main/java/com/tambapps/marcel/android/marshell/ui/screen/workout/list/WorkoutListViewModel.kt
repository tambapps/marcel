package com.tambapps.marcel.android.marshell.ui.screen.workout.list

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkout
import com.tambapps.marcel.android.marshell.workout.ShellWorkoutManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WorkoutListViewModel @Inject constructor(
  private val shellWorkoutManager: ShellWorkoutManager
): ViewModel() {

  init {
    CoroutineScope(Dispatchers.IO).launch {
      refresh()
    }
  }

  val works = mutableStateListOf<ShellWorkout>()

  suspend fun refresh() {
    val list = shellWorkoutManager.list().sortedWith(
      compareBy<ShellWorkout> { it.state.isFinished }
        .thenByDescending { it.createdAt })
    withContext(Dispatchers.Main) {
      works.clear()
      works.addAll(list)
    }
  }
}