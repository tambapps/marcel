package com.tambapps.marcel.android.marshell.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tambapps.marcel.android.marshell.room.converter.Converters
import com.tambapps.marcel.android.marshell.room.dao.MessageDao
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkoutDao
import com.tambapps.marcel.android.marshell.room.entity.Message
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkout

@TypeConverters(Converters::class)
@Database(entities = [ShellWorkout::class, Message::class], version = 8)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shellWorkDataDao(): ShellWorkoutDao
    abstract fun messageDao(): MessageDao
}