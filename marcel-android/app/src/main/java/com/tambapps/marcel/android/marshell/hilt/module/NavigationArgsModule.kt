package com.tambapps.marcel.android.marshell.hilt.module

import androidx.lifecycle.SavedStateHandle
import com.tambapps.marcel.android.marshell.Routes
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkout
import com.tambapps.marcel.android.marshell.workout.ShellWorkoutManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.URLDecoder

/**
 * Module allowing to pass workout argument from navigation to WorkViewModel
 */
@Module
@InstallIn(ViewModelComponent::class)
class NavigationArgsModule {

  /**
   * Method allowing to pass workout argument from navigation to WorkViewModel
   */
  @Provides
  @ViewModelScoped
  fun workout(
    savedStateHandle: SavedStateHandle,
    shellWorkoutManager: ShellWorkoutManager
  ): ShellWorkout? {
    return savedStateHandle.get<String>(Routes.WORKOUT_NAME_ARG)?.let {
      runBlocking { shellWorkoutManager.findByName(it) }
    }
  }

  /**
   * method allowing to pass optional file argument from navigation to EditorViewModel
   */
  @Provides
  @ViewModelScoped
  fun file(
    savedStateHandle: SavedStateHandle,
  ): File? {
    return savedStateHandle.get<String>(Routes.FILE_ARG)?.let {
      File(URLDecoder.decode(it, "UTF-8"))
    }
  }

  /**
   * method allowing to pass optional file argument from navigation to DocumentationViewModel
   */
  @Provides
  @ViewModelScoped
  fun path(
    savedStateHandle: SavedStateHandle,
  ): String? {
    return savedStateHandle.get<String>(Routes.PATH_ARG)?.let {
      URLDecoder.decode(it, "UTF-8")
    }
  }
}