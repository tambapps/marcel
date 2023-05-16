package com.tambapps.marcel.android.marshell.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDataDao
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkData

@Database(entities = [ShellWorkData::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shellWorkDataDao(): ShellWorkDataDao
}