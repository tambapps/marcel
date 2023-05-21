package com.tambapps.marcel.android.marshell.room.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime

// automatically used by room
class LocalDateTimeConverter {

  @TypeConverter
  fun localDateTimeFromString(s: String?) = s?.let(LocalDateTime::parse)

  @TypeConverter
  fun stringToLocalDateTime(localDateTime: LocalDateTime?) = localDateTime?.toString()
}