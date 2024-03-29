package com.tambapps.marcel.android.marshell.configuration

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.work.WorkManager
import com.tambapps.marcel.android.marshell.maven.DexRemoteSavingRepository
import com.tambapps.marcel.android.marshell.room.AppDatabase
import com.tambapps.marcel.android.marshell.room.dao.CacheableScriptDao
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDao
import com.tambapps.marcel.compiler.CompilerConfiguration
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

  @Named("dumbbellRootFile")
  @Provides
  fun dumbbellRootFile(@ApplicationContext context: Context): File {
    return context.getDir("dumbbell", Context.MODE_PRIVATE)
  }

  @Provides
  fun dumbbellMavenRepository(@Named("dumbbellRootFile") dumbbellRootFile: File): RemoteSavingMavenRepository {
    return DexRemoteSavingRepository(dumbbellRootFile)
  }

  @Provides
  fun compilerConfiguration(): CompilerConfiguration {
    return CompilerConfiguration(dumbbellEnabled = true, classVersion = 52) // Java 8
  }

  @Provides
  fun appDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(
      context,
      AppDatabase::class.java, "marshell_android"
    ).build()
  }

  @Provides
  fun shellWorkDataDao(appDatabase: AppDatabase): ShellWorkDao {
    return appDatabase.shellWorkDataDao()
  }

  @Provides
  fun cacheableScriptDao(appDatabase: AppDatabase): CacheableScriptDao {
    return appDatabase.cacheableScriptDao()
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