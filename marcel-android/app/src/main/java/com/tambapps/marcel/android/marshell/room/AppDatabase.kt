package com.tambapps.marcel.android.marshell.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tambapps.marcel.android.marshell.room.converter.LocalDateTimeConverter
import com.tambapps.marcel.android.marshell.room.converter.WorkPeriodConverter
import com.tambapps.marcel.android.marshell.room.dao.CacheableScriptDao
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDao
import com.tambapps.marcel.android.marshell.room.entity.CacheableScript
import com.tambapps.marcel.android.marshell.room.entity.ShellWork

@TypeConverters(LocalDateTimeConverter::class, WorkPeriodConverter::class)
@Database(entities = [ShellWork::class, CacheableScript::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shellWorkDataDao(): ShellWorkDao
    abstract fun cacheableScriptDao(): CacheableScriptDao
}