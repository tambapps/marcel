package com.tambapps.marcel.android.marshell.room.converter

import androidx.room.TypeConverter
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriod
import java.time.LocalDateTime


// automatically used by room
class Converters {

  @TypeConverter
  fun localDateTimeFromString(s: String?) = s?.let(LocalDateTime::parse)

  @TypeConverter
  fun localDateTimeToString(localDateTime: LocalDateTime?) = localDateTime?.toString()

  @TypeConverter
  fun stringListFromString(s: String?) = s?.split(",")

  @TypeConverter
  fun stringListToString(list: List<String>?) = list?.joinToString(",")

  @TypeConverter
  fun workPeriodFromString(s: String?) = s?.let(WorkPeriod::parse)

  @TypeConverter
  fun workPeriodToString(workPeriod: WorkPeriod?) = workPeriod?.toString()

}