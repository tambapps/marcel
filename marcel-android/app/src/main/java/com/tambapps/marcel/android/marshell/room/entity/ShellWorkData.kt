package com.tambapps.marcel.android.marshell.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.work.WorkInfo.State
import com.tambapps.marcel.android.marshell.ui.shellwork.form.PeriodUnit
import java.util.UUID

// room only supports TEXT, INTEGER, BLOB, REAL and UNDEFINED
@Entity("shell_work_data")
data class ShellWorkData(
  @PrimaryKey val id: UUID,
  @ColumnInfo val name: String,
  @ColumnInfo val description: String?,
  @ColumnInfo val silent: Boolean,
  @ColumnInfo(name = "state") val state: State,
  @ColumnInfo(name = "period_amount") val periodAmount: Int?,
  @ColumnInfo(name = "period_unit") val periodUnit: PeriodUnit?,
  @ColumnInfo(name = "start_time") val startTime: String?,
  @ColumnInfo(name = "end_time") val endTime: String?,
  @ColumnInfo(name = "scheduled_at") val scheduledAt: String?,
  @ColumnInfo val logs: String?,
  @ColumnInfo val result: String?,
  @ColumnInfo(name = "failure_reason") val failedReason: String?,
  // not fetched by default. Only fetched for the byId
  @ColumnInfo(name = "script_text") val scriptText: String?,
  ) {
  val isPeriodic get() = periodAmount != null && periodUnit != null

}