package com.tambapps.marcel.android.marshell.hilt.module

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.work.WorkManager
import com.tambapps.marcel.android.compiler.BuildConfig
import com.tambapps.marcel.android.marshell.maven.DexRemoteSavingRepository
import com.tambapps.marcel.android.marshell.repl.MarshellScript
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.dumbbell.DumbbellEngine
import com.tambapps.marcel.semantic.SemanticPurpose
import com.tambapps.maven.dependency.resolver.repository.RemoteSavingMavenRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
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

  @Named("workSessionsDirectory")
  @Provides
  fun workSessionsDirectory(@ApplicationContext context: Context): File {
    // TODO use cacheDir for work sessions because they are supposed to be ephemeral and can be deleted safely
    return context.getDir("work_sessions", Context.MODE_PRIVATE)
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
      classVersion = CompilerConfiguration.getClassVersion(BuildConfig.JAVA_VERSION),
      scriptClass = MarshellScript::class.java,
      purpose = SemanticPurpose.REPL
    )
  }

  @Provides
  @Singleton // important. There must be only one instance of this or else it will crash
  fun dataStore(@ApplicationContext context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("preferences") }
  }

  @Provides
  fun dumbbellMavenRepository(@Named("dumbbellRootFile") dumbbellRootFile: File): RemoteSavingMavenRepository {
    return DexRemoteSavingRepository(dumbbellRootFile)
  }

  @Provides
  fun dumbbellEngine(dumbbellMavenRepository: RemoteSavingMavenRepository): DumbbellEngine = DumbbellEngine(dumbbellMavenRepository)

  @Provides
  fun workManager(@ApplicationContext context: Context) = WorkManager.getInstance(context)

  @Provides
  fun okHttp() = OkHttpClient()

  @Named("documentationCacheDir")
  @Provides
  fun documentationCacheDir(@ApplicationContext context: Context): File {
    return File(context.cacheDir, "md_doc")
  }
}