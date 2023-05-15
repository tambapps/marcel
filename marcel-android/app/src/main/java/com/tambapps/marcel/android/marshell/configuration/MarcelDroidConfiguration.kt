package com.tambapps.marcel.android.marshell.configuration

import android.content.Context
import android.content.SharedPreferences
import androidx.hilt.work.HiltWorkerFactory
import androidx.preference.PreferenceManager
import androidx.work.Configuration
import androidx.work.WorkManager
import com.tambapps.marcel.compiler.CompilerConfiguration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Named

@Module
@InstallIn(ActivityComponent::class, FragmentComponent::class,
  // for workers
  SingletonComponent::class)
class MarcelDroidConfiguration {

  @Named("initScriptFile")
  @Provides
  fun initScriptFile(@ApplicationContext context: Context): File {
    val dir = context.getDir("configuration", Context.MODE_PRIVATE)
    return File(dir, "init.mcl")
  }

  @Named("shellSessionsDirectory")
  @Provides
  fun shellSessionsDirectory(@ApplicationContext context: Context): File {
    return context.getDir("shell_sessions", Context.MODE_PRIVATE)
  }

  @Provides
  fun compilerConfiguration(): CompilerConfiguration {
    return CompilerConfiguration(dumbbellEnabled = true, classVersion = 52) // Java 8
  }

  @Provides
  fun sharedPreferences(@ApplicationContext context: Context): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(context)
  }

  @Provides
  fun workManager(@ApplicationContext context: Context): WorkManager {
    return WorkManager.getInstance(context)
  }
}