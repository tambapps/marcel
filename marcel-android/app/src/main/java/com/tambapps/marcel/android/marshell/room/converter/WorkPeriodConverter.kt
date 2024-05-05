package com.tambapps.marcel.android.marshell.room.converter

import androidx.room.TypeConverter
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriod

// automatically used by room
class WorkPeriodConverter {

  @TypeConverter
  fun workPeriodFromString(s: String?) = s?.let(WorkPeriod::parse)

  @TypeConverter
  fun workPeriodToString(workPeriod: WorkPeriod?) = workPeriod?.toString()
}