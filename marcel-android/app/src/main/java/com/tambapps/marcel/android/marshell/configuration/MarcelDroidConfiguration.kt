package com.tambapps.marcel.android.marshell.configuration

import android.content.Context
import com.tambapps.marcel.compiler.CompilerConfiguration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Named

@Module
@InstallIn(ActivityComponent::class, FragmentComponent::class)
class MarcelDroidConfiguration {

  @Named("initScriptFile")
  @Provides
  fun initScriptFile(@ApplicationContext context: Context): File {
    val dir = context.getDir("configuration", Context.MODE_PRIVATE)
    return File(dir, "init.mcl")
  }

  @Provides
  fun compilerConfiguration(): CompilerConfiguration {
    return CompilerConfiguration(dumbbellEnabled = true, classVersion = 52) // Java 8
  }

}