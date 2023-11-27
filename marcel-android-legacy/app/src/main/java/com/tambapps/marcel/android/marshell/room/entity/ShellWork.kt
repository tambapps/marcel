package com.tambapps.marcel.android.marshell.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.work.WorkInfo.State
import com.tambapps.marcel.android.marshell.ui.shellwork.form.PeriodUnit
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Entity("shell_works")
data class ShellWork(
  @PrimaryKey val name: String, // yes, works are unique by name and their "id" may vary
  @ColumnInfo(name = "work_id") val workId: UUID,
  @ColumnInfo val description: String?,
  @ColumnInfo(name = "is_network_required") val isNetworkRequired: Boolean,
  @ColumnInfo(name = "is_silent") val isSilent: Boolean,
  @ColumnInfo(name = "state") val state: State,
  @ColumnInfo(name = "period_amount") val periodAmount: Int?,
  @ColumnInfo(name = "period_unit") val periodUnit: PeriodUnit?,
  @ColumnInfo(name = "start_time") val startTime: LocalDateTime?,
  @ColumnInfo(name = "end_time") val endTime: LocalDateTime?,
  @ColumnInfo(name = "scheduled_at") val scheduledAt: LocalDateTime?,
  @ColumnInfo val logs: String?,
  @ColumnInfo val result: String?,
  @ColumnInfo(name = "failure_reason") val failedReason: String?,
  // not fetched by default. Only fetched for the byId
  @ColumnInfo(name = "script_text") val scriptText: String?,
) {

  @Ignore
  val isFinished = state.isFinished
  @Ignore
  val isPeriodic = periodAmount != null && periodUnit != null

  val durationBetweenNowAndNext: Duration?
    get() {
      if (endTime == null || periodUnit == null || periodAmount == null) return null
    return Duration.between(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), endTime.plusMinutes(periodUnit.toMinutes(periodAmount)))
  }

}