package com.tambapps.marcel.android.marshell.work

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

data class MarcelShellWorkInfo(
  val id: UUID, val state: State, val name: String, val description: String?,
  val periodAmount: Long?, val periodUnit: PeriodUnit?, val isFinished: Boolean,
  val startTime: LocalDateTime?, val endTime: LocalDateTime?, val scheduledAt: LocalDateTime?,
  val output: String?,
  val result: String?,
  val failedReason: String?) {
  val isPeriodic get() = periodAmount != null && periodUnit != null
  val startTimeFormatted get() = startTime?.format(DATE_FORMATTER)
  val endTimeFormatted get() = endTime?.format(DATE_FORMATTER)

  val durationBetweenNowAndNext: Duration get() {
    return Duration.between(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), endTime!!.plusMinutes(periodUnit!!.toMinutes(periodAmount!!)))
  }


  companion object {
    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

    internal const val START_TIME_KEY = "startTime"
    internal const val END_TIME_KEY = "endTime"
    internal const val OUTPUT_KEY = "output"
    internal const val RESULT_KEY = "result"
    internal const val FAILED_REASON_KEY = "failedReason"

    fun fromWorkInfo(workInfo: WorkInfo): MarcelShellWorkInfo {
      val tags = workInfo.tags
      val data = workInfo.outputData
      val startTimeMillis = data.getLong(START_TIME_KEY, -1)
      val endTimeMillis = data.getLong(END_TIME_KEY, -1)
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
        scheduledAt = WorkTags.getSchedule(tags),
        output = data.getString(OUTPUT_KEY),
        result = data.getString(RESULT_KEY),
        failedReason = data.getString(FAILED_REASON_KEY)
      )
    }
  }
}