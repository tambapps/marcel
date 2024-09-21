package com.tambapps.marcel.android.marshell.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tambapps.marcel.android.marshell.room.converter.Converters
import com.tambapps.marcel.android.marshell.room.dao.MessageDao
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDao
import com.tambapps.marcel.android.marshell.room.entity.Message
import com.tambapps.marcel.android.marshell.room.entity.ShellWork

@TypeConverters(Converters::class)
@Database(entities = [ShellWork::class, Message::class], version = 7)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shellWorkDataDao(): ShellWorkDao
    abstract fun messageDao(): MessageDao
}