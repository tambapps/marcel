package com.tambapps.marcel.android.marshell.room.entity

enum class WorkPeriodUnit {
  MINUTES, HOURS, DAYS;

  fun toMinutes(n: Int): Long {
    return n * when (this) {
      MINUTES -> 1L
      HOURS -> 60L
      DAYS -> HOURS.toMinutes(24)
    }
  }

  override fun toString(): String {
    return super.toString().lowercase()
  }
}
