package com.tambapps.marcel.android.marshell.work

import androidx.work.WorkInfo.State
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkData
import com.tambapps.marcel.android.marshell.ui.shellwork.form.PeriodUnit
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

data class ShellWork(
  val id: UUID,
  val state: State,
  val name: String,
  val silent: Boolean,
  val scriptText: String?,
  val description: String?,
  val periodAmount: Int?,
  val periodUnit: PeriodUnit?,
  val startTime: LocalDateTime?,
  val endTime: LocalDateTime?,
  val scheduledAt: LocalDateTime?,
  val logs: String?,
  val result: String?,
  val failedReason: String?,
  // TODO get it from data
  val isNetworkRequired: Boolean = false
) {

  val isFinished get() = state.isFinished
  val isPeriodic get() = periodAmount != null && periodUnit != null

  val durationBetweenNowAndNext: Duration?
    get() {
      if (endTime == null || periodUnit == null || periodAmount == null) return null
    return Duration.between(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), endTime.plusMinutes(periodUnit.toMinutes(periodAmount)))
  }

  companion object {

    fun from(data: ShellWorkData): ShellWork {
      return ShellWork(
        id = data.id,
        state = data.state,
        name = data.name,
        scriptText = data.scriptText,
        silent = data.silent,
        description = data.description,
        periodAmount = data.periodAmount,
        periodUnit = data.periodUnit,
        startTime = data.startTime?.let(LocalDateTime::parse),
        endTime = data.endTime?.let(LocalDateTime::parse),
        scheduledAt = data.scheduledAt?.let(LocalDateTime::parse),
        logs = data.logs,
        result = data.result,
        failedReason = data.failedReason
      )
    }
  }
}