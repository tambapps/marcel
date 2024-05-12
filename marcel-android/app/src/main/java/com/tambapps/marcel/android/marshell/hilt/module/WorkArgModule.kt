package com.tambapps.marcel.android.marshell.hilt.module

import androidx.lifecycle.SavedStateHandle
import com.tambapps.marcel.android.marshell.Routes
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.work.ShellWorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.runBlocking

/**
 * Module allowing to pass work argument from navigation to WorkViewModel
 */
@Module
@InstallIn(ViewModelComponent::class)
class WorkArgModule {

  @Provides
  @ViewModelScoped
  fun work(
    savedStateHandle: SavedStateHandle,
    shellWorkManager: ShellWorkManager
  ): ShellWork? {
    return savedStateHandle.get<String>(Routes.WORK_NAME_ARG)?.let {
      runBlocking { shellWorkManager.findByName(it) }
    }
  }
}