package com.tambapps.marcel.android.marshell.room.entity

enum class WorkPeriodUnit {
  MINUTES, HOURS, DAYS, WEEKS, THIRTY_DAYS {
    override fun toString(): String {
      return "30 days"
    }
  };

  fun toMinutes(n: Int): Long {
    return n * when (this) {
      MINUTES -> 1L
      HOURS -> 60L
      DAYS -> HOURS.toMinutes(24)
      WEEKS -> DAYS.toMinutes(7)
      THIRTY_DAYS -> DAYS.toMinutes(30)
    }
  }

  override fun toString(): String {
    return super.toString().lowercase()
  }
}
