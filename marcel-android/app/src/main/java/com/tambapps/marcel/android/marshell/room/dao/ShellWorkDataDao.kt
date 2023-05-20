package com.tambapps.marcel.android.marshell.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.work.WorkInfo.State
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkData
import java.time.LocalDateTime
import java.util.UUID

@Dao
interface ShellWorkDataDao {

  companion object {
    const val MAIN_COLUMNS = "id, name, description, silent, period_amount, period_unit, state, " +
        "start_time, end_time, scheduled_at, logs, result, failure_reason"
  }

  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Query("SELECT $MAIN_COLUMNS FROM shell_work_data")
  suspend fun findAll(): List<ShellWorkData>

  // fetched everything, including the script for the by id
  @Query("SELECT * FROM shell_work_data WHERE id = :id")
  suspend fun findById(id: UUID): ShellWorkData?

  @Insert
  suspend fun insert(data: ShellWorkData)

  @Delete
  suspend fun delete(data: ShellWorkData)

  @Query("UPDATE shell_work_data SET failure_reason = :failureReason WHERE id =:id")
  suspend fun updateFailureReason(id: UUID, failureReason: String)

  @Query("UPDATE shell_work_data SET result = :result WHERE id =:id")
  suspend fun updateResult(id: UUID, result: String)

  @Query("UPDATE shell_work_data SET state = :state WHERE id =:id")
  suspend fun updateState(id: UUID, state: State)

  suspend fun updateStartTime(id: UUID, startTime: LocalDateTime) {
    return updateStartTime(id, startTime.toString())
  }

  @Query("UPDATE shell_work_data SET start_time = :startTime WHERE id =:id")
  suspend fun updateStartTime(id: UUID, startTime: String)

  suspend fun updateEndTime(id: UUID, endTime: LocalDateTime) {
    return updateEndTime(id, endTime.toString())
  }

  @Query("UPDATE shell_work_data SET end_time = :endTime WHERE id =:id")
  suspend fun updateEndTime(id: UUID, endTime: String)
}