package com.tambapps.marcel.android.marshell.work

import android.util.Log
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State
import com.tambapps.marcel.android.marshell.ui.shellwork.form.PeriodUnit
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

data class MarcelShellWorkInfo(val id: UUID, val state: State, val name: String, val description: String?,
val periodAmount: Long?, val periodUnit: PeriodUnit?, val isFinished: Boolean, val startTime: LocalDateTime?, val endTime: LocalDateTime?,
                               val scheduledAt: LocalDateTime?) {
  val isPeriodic get() = periodAmount != null && periodUnit != null
  val startTimeFormatted get() = startTime?.format(DATE_FORMATTER)
  val endTimeFormatted get() = endTime?.format(DATE_FORMATTER)

  val durationBetweenNowAndNext: Duration get() {
    return Duration.between(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), endTime!!.plusMinutes(periodUnit!!.toMinutes(periodAmount!!)))
  }


  companion object {
    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

    fun fromWorkInfo(workInfo: WorkInfo): MarcelShellWorkInfo {
      val tags = workInfo.tags
      val data = workInfo.outputData
      val startTimeMillis = data.getLong("startTime", -1)
      val endTimeMillis = data.getLong("endTime", -1)
      return MarcelShellWorkInfo(
        id = workInfo.id,
        state = workInfo.state,
        name = WorkTags.getName(tags) ?: "<no name>",
        description = WorkTags.getDescription(tags),
        periodUnit = WorkTags.getPeriodUnit(tags),
        periodAmount = WorkTags.getPeriodAmount(tags),
        isFinished = workInfo.state.isFinished,
        startTime = if (startTimeMillis != -1L) LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeMillis), ZoneId.systemDefault()) else null,
        endTime = if (endTimeMillis != -1L) LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimeMillis), ZoneId.systemDefault()) else null,
        scheduledAt = WorkTags.getSchedule(tags)
      )
    }
  }
}