package com.tambapps.marcel.android.marshell.hilt.module

import androidx.lifecycle.SavedStateHandle
import com.tambapps.marcel.android.marshell.Routes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import java.io.File
import java.net.URLDecoder

/**
 * Module allowing to pass optional file argument from navigation to EditorViewModel
 */
@Module
@InstallIn(ViewModelComponent::class)
class EditorFileArgModule {

  @Provides
  @ViewModelScoped
  fun file(
    savedStateHandle: SavedStateHandle,
  ): File? {
    return savedStateHandle.get<String>(Routes.FILE_ARG)?.let {
      File(URLDecoder.decode(it, "UTF-8"))
    }
  }
}