package com.tambapps.marcel.android.marshell.room.entity

data class WorkPeriod(val amount: Int, val unit: WorkPeriodUnit) {

  companion object {
    fun parse(s: String) = s.split(Regex("\\s+"))
      .let { WorkPeriod(it.first().toInt(), WorkPeriodUnit.valueOf(it.last())) }
  }
  fun toMinutes() = unit.toMinutes(amount)

  override fun toString() = "$amount $unit"
}
