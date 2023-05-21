package com.tambapps.marcel.android.marshell.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tambapps.marcel.android.marshell.room.converter.LocalDateTimeConverter
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDao
import com.tambapps.marcel.android.marshell.room.entity.ShellWork

@TypeConverters(LocalDateTimeConverter::class)
@Database(entities = [ShellWork::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shellWorkDataDao(): ShellWorkDao
}