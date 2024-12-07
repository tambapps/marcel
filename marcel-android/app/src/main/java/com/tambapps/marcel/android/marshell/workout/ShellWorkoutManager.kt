package com.tambapps.marcel.android.marshell.workout

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.WorkRequest
import com.google.common.util.concurrent.ListenableFuture
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkoutDao
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkout
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriod
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShellWorkoutManager @Inject constructor(
  private val workManager: WorkManager,
  private val shellWorkoutDao: ShellWorkoutDao
) {

  companion object {
    private const val SHELL_WORK_TAG = "type:shell_work"
    private const val NAME_TAG_PREFIX = "name:"

    fun getName(tags: Set<String>) = tags.find { it.startsWith(NAME_TAG_PREFIX) }?.substring(NAME_TAG_PREFIX.length)
  }

  suspend fun list(): List<ShellWorkout> = shellWorkoutDao.findAll()

  suspend fun findByName(name: String) = shellWorkoutDao.findByName(name)

  private fun listWorkInfo(): ListenableFuture<List<WorkInfo>> {
    return workManager.getWorkInfos(WorkQuery.fromTags(SHELL_WORK_TAG))
  }
  fun existsByName(name: String): Boolean {
    val works = listWorkInfo().get()
    return works.any { it.tags.contains("name:$name") }
  }

  suspend fun update(name: String, scriptText: String?): ShellWorkout? {
    if (scriptText != null) {
      shellWorkoutDao.updateScriptText(name, scriptText)
    }
    return shellWorkoutDao.findByName(name)
  }

  /**
   * Upsert a ShellWorkout (the ID being the name)
   */
  suspend fun save(
    name: String,
    description: String?,
    scriptText: String,
    period: WorkPeriod?,
    scheduleAt: LocalDateTime?,
    requiresNetwork: Boolean,
    initScripts: List<String>?
  ) {
    val operation = doWorkRequest(name, period, requiresNetwork, scheduleAt)
    // waiting for the workout to be created
    operation.result.get()

    // useful in order not to erase existing data when updating an existing periodic work
    val currentWork = findByName(name)?.takeIf { it.isPeriodic }
    val workId = listWorkInfo().get().find { it.tags.contains("name:$name") }!!.id
    // now create shell_work_data
    val data = ShellWorkout(
      workId = workId,
      isNetworkRequired = requiresNetwork,
      name = name,
      description = description,
      period = period,
      scheduledAt = scheduleAt,
      scriptText = scriptText,
      state = WorkInfo.State.ENQUEUED,
      createdAt = LocalDateTime.now(),
      lastUpdatedAt = LocalDateTime.now(),
      startTime = currentWork?.startTime, endTime = currentWork?.endTime,
      logs = currentWork?.logs, result = currentWork?.result, resultClassName = currentWork?.resultClassName, failedReason = currentWork?.failedReason, initScripts = initScripts)
    shellWorkoutDao.upsert(data)
  }

  private fun doWorkRequest(
    name: String,
    period: WorkPeriod?,
    requiresNetwork: Boolean,
    scheduleAt: LocalDateTime? = null
  ): Operation {
    val workRequest: WorkRequest.Builder<*, *> =
      if (period != null) PeriodicWorkRequestBuilder<ShellWorkoutWorker>(period.toMinutes(), TimeUnit.MINUTES)
        .setInitialDelay(scheduleAt?.let { Duration.between(LocalDateTime.now(), it) } ?: Duration.ZERO)
      else OneTimeWorkRequest.Builder(ShellWorkoutWorker::class.java)
    workRequest.addTag(SHELL_WORK_TAG)
      // useful to check uniqueness without fetching shell workouts, and to fetch workout from workout
      .addTag("$NAME_TAG_PREFIX$name")

    if (requiresNetwork) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
      workRequest.setConstraints(constraints)
    }
    workRequest.setInputData(Data.Builder().build())

    return if (workRequest is PeriodicWorkRequest.Builder) workManager.enqueueUniquePeriodicWork(name, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, workRequest.build())
    else workManager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, workRequest.build() as OneTimeWorkRequest)
  }

  suspend fun cancel(name: String): ShellWorkout? {
    workManager.cancelUniqueWork(name).result.get()
    shellWorkoutDao.updateState(name, WorkInfo.State.CANCELLED)
    return findByName(name)
  }

  suspend fun delete(name: String): Boolean {
    workManager.cancelUniqueWork(name).result.get()
    val data = shellWorkoutDao.findByName(name) ?: return false
    shellWorkoutDao.delete(data)
    return true
  }

  suspend fun runLateWorkouts() {
    val lateWorkouts = list().filter { workout ->
      workout.durationBetweenNowAndNext?.let { it.isNegative && it.abs().toMinutes() >= 10L } ?: false
    }
    lateWorkouts.forEach {
      val operation = doWorkRequest(it.name, it.period, it.isNetworkRequired)
      println(operation.result.get())
    }
  }
}