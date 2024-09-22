package com.tambapps.marcel.android.marshell.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Update
import androidx.room.Upsert
import androidx.work.WorkInfo.State
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkout
import java.time.LocalDateTime
import java.util.UUID

@Dao
interface ShellWorkoutDao {

  companion object {
    // we don't fetch scriptText and logs for list endpoint
    const val MAIN_COLUMNS = "work_id, name, description, period, state, created_at, last_updated_at," +
        "start_time, end_time, scheduled_at, result, failure_reason, is_network_required, init_scripts"
  }

  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Query("SELECT $MAIN_COLUMNS FROM shell_workouts")
  suspend fun findAll(): List<ShellWorkout>

  // fetched everything, including the script
  @Query("SELECT * FROM shell_workouts WHERE work_id = :id")
  suspend fun findById(id: UUID): ShellWorkout?

  // fetched everything, including the script
  @Query("SELECT * FROM shell_workouts WHERE name = :name")
  suspend fun findByName(name: String): ShellWorkout?

  @Upsert
  suspend fun upsert(data: ShellWorkout)
  @Insert
  suspend fun insert(data: ShellWorkout)

  @Update
  suspend fun update(data: ShellWorkout)

  @Delete
  suspend fun delete(data: ShellWorkout)

  @Query("UPDATE shell_workouts SET failure_reason = :failureReason WHERE name =:name")
  suspend fun updateFailureReason(name: String, failureReason: String?)

  @Query("UPDATE shell_workouts SET state = :state WHERE name =:name")
  suspend fun updateState(name: String, state: State)

  @Query("UPDATE shell_workouts SET start_time = :startTime WHERE name =:name")
  suspend fun updateStartTime(name: String, startTime: LocalDateTime)

  @Query("UPDATE shell_workouts SET end_time = :endTime WHERE name =:name")
  suspend fun updateEndTime(name: String, endTime: LocalDateTime)
  @Query("UPDATE shell_workouts SET logs = :logs WHERE name =:name")
  suspend fun updateLogs(name: String, logs: String)
  @Query("UPDATE shell_workouts SET script_text = :scriptText, last_updated_at = :lastUpdatedAt WHERE name =:name")
  suspend fun updateScriptText(name: String, scriptText: String, lastUpdatedAt: LocalDateTime)
  suspend fun updateScriptText(name: String, scriptText: String) = updateScriptText(name, scriptText, LocalDateTime.now())

  @Query("UPDATE shell_workouts SET end_time = :endTime, result = :result, result_class_name = :resultClassName, failure_reason = :failureReason, logs = :logs, state = :state WHERE name =:name")
  suspend fun update(name: String, endTime: LocalDateTime, result: String?, resultClassName: String?, failureReason: String?, logs: String, state: State)
}