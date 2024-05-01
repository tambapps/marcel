package com.tambapps.marcel.android.marshell.room.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime

// automatically used by room
class LocalDateTimeConverter {

  @TypeConverter
  fun localDateTimeFromString(s: String?) = s?.let(LocalDateTime::parse)

  @TypeConverter
  fun localDateTimeToString(localDateTime: LocalDateTime?) = localDateTime?.toString()

  @TypeConverter
  fun stringListFromString(s: String?) = s?.split(",")

  @TypeConverter
  fun stringListToString(list: List<String>?) = list?.joinToString(",")
}