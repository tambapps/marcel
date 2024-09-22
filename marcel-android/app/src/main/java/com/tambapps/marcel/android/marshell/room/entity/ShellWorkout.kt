package com.tambapps.marcel.android.marshell.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.work.WorkInfo.State
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Entity("shell_workouts")
data class ShellWorkout(
  @PrimaryKey val name: String, // yes, works are unique by name and their "id" may vary
  @ColumnInfo(name = "work_id") val workId: UUID,
  @ColumnInfo val description: String?,
  @ColumnInfo(name = "is_network_required") val isNetworkRequired: Boolean,
  @ColumnInfo(name = "state") val state: State,
  @ColumnInfo(name = "created_at") val createdAt: LocalDateTime,
  @ColumnInfo(name = "last_updated_at") val lastUpdatedAt: LocalDateTime,
  @ColumnInfo(name = "period") val period: WorkPeriod?,
  @ColumnInfo(name = "start_time") val startTime: LocalDateTime?,
  @ColumnInfo(name = "end_time") val endTime: LocalDateTime?,
  @ColumnInfo(name = "scheduled_at") val scheduledAt: LocalDateTime?,
  @ColumnInfo val logs: String?,
  @ColumnInfo val result: String?,
  @ColumnInfo(name = "result_class_name") val resultClassName: String?,
  @ColumnInfo(name = "failure_reason") val failedReason: String?,
  @ColumnInfo(name = "init_scripts") val initScripts: List<String>?,
  // not fetched by default. Only fetched for the byId
  @ColumnInfo(name = "script_text") val scriptText: String?,
) {

  @Ignore
  val isFinished = state.isFinished
  @Ignore
  val isPeriodic = period != null

  val durationBetweenNowAndNext: Duration?
    get() {
      if (endTime == null || period == null) return null
    return Duration.between(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), endTime.plusMinutes(period.toMinutes()))
  }

}