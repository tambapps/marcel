package com.tambapps.marcel.android.marshell.hilt.module

import android.content.Context
import androidx.room.Room
import com.tambapps.marcel.android.marshell.room.AppDatabase
import com.tambapps.marcel.android.marshell.room.dao.MessageDao
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
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
    )
      .fallbackToDestructiveMigration()
      .build()
  }

  @Provides
  fun shellWorkDataDao(appDatabase: AppDatabase): ShellWorkoutDao {
    return appDatabase.shellWorkDataDao()
  }

  @Provides
  fun messageDao(appDatabase: AppDatabase): MessageDao {
    return appDatabase.messageDao()
  }
}