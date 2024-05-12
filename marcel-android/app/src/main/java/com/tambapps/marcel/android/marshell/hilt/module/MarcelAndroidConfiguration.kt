package com.tambapps.marcel.android.marshell.hilt.module

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.work.WorkManager
import com.tambapps.marcel.android.marshell.maven.DexRemoteSavingRepository
import com.tambapps.marcel.android.marshell.repl.MarshellScript
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.dumbbell.DumbbellEngine
import com.tambapps.maven.dependency.resolver.repository.RemoteSavingMavenRepository
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
@InstallIn(ActivityComponent::class,
  // for workers
  SingletonComponent::class)
class MarcelAndroidConfiguration {

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

  @Named("dumbbellRootFile")
  @Provides
  fun dumbbellRootFile(@ApplicationContext context: Context): File {
    return context.getDir("dumbbell", Context.MODE_PRIVATE)
  }

  @Provides
  fun compilerConfiguration(): CompilerConfiguration {
    return CompilerConfiguration(
      dumbbellEnabled = true,
      classVersion = CompilerConfiguration.VERSION_MAP["17"]!!, // Java 8
      scriptClass = MarshellScript::class.java
    )
  }

  @Provides
  fun dumbbellMavenRepository(@Named("dumbbellRootFile") dumbbellRootFile: File): RemoteSavingMavenRepository {
    return DexRemoteSavingRepository(dumbbellRootFile)
  }

  @Provides
  fun dumbbellEngine(dumbbellMavenRepository: RemoteSavingMavenRepository): DumbbellEngine = DumbbellEngine(dumbbellMavenRepository)

  @Provides
  fun dataStore(@ApplicationContext context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("preferences") }
  }

  @Provides
  fun workManager(@ApplicationContext context: Context) = WorkManager.getInstance(context)
}