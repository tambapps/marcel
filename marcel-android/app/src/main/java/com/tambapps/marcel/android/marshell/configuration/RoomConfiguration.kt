package com.tambapps.marcel.android.marshell.configuration

import android.content.Context
import androidx.room.Room
import com.tambapps.marcel.android.marshell.room.AppDatabase
import com.tambapps.marcel.android.marshell.room.dao.CacheableScriptDao
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(
  ActivityComponent::class, FragmentComponent::class,
  // for workers
  SingletonComponent::class)
class RoomConfiguration {

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
}