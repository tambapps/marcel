package com.tambapps.marcel.android.marshell.work

import androidx.work.WorkInfo
import androidx.work.WorkInfo.State
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkData
import com.tambapps.marcel.android.marshell.ui.shellwork.form.PeriodUnit
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

data class ShellWork(
  val id: UUID,
  val state: State,
  val name: String,
  val silent: Boolean,
  val scriptFile: File,
  val description: String?,
  val periodAmount: Int?,
  val periodUnit: PeriodUnit?,
  val startTime: LocalDateTime?,
  val endTime: LocalDateTime?,
  val scheduledAt: LocalDateTime?,
  val output: String?,
  val result: String?,
  val failedReason: String?
) {

  val isFinished get() = state.isFinished
  val isPeriodic get() = periodAmount != null && periodUnit != null
  val startTimeFormatted get() = startTime?.format(DATE_FORMATTER)
  val endTimeFormatted get() = endTime?.format(DATE_FORMATTER)

  val durationBetweenNowAndNext: Duration
    get() {
    return Duration.between(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), endTime!!.plusMinutes(periodUnit!!.toMinutes(periodAmount!!)))
  }

  companion object {
    const val SHELL_WORK_TYPE = "shell_work"
    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

    fun from(info: WorkInfo, data: ShellWorkData): ShellWork {
      return ShellWork(
        id = info.id,
        state = info.state,
        name = data.name,
        scriptFile = File(data.scriptFilePath),
        silent = data.silent,
        description = data.description,
        periodAmount = data.periodAmount,
        periodUnit = data.periodUnit,
        startTime = data.startTime?.let(LocalDateTime::parse),
        endTime = data.endTime?.let(LocalDateTime::parse),
        scheduledAt = data.scheduledAt?.let(LocalDateTime::parse),
        output = data.output,
        result = data.output,
        failedReason = data.failedReason
      )
    }
  }
}