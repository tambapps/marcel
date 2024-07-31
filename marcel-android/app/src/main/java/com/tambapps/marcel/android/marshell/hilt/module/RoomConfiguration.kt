package com.tambapps.marcel.android.marshell.hilt.module

import android.content.Context
import androidx.room.Room
import com.tambapps.marcel.android.marshell.room.AppDatabase
import com.tambapps.marcel.android.marshell.room.dao.CacheableScriptDao
import com.tambapps.marcel.android.marshell.room.dao.MessageDao
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDao
import com.tambapps.marcel.android.marshell.room.migration.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(
  ActivityComponent::class,
  // for workers
  SingletonComponent::class)
class RoomConfiguration {

  @Provides
  fun appDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(
      context,
      AppDatabase::class.java, "marshell_android"
    ).addMigrations(MIGRATION_1_2).build()
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
  fun messageDao(appDatabase: AppDatabase): MessageDao {
    return appDatabase.messageDao()
  }
}