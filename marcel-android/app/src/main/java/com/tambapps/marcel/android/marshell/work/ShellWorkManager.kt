package com.tambapps.marcel.android.marshell.work

import android.content.Context
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
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDao
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriod
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShellWorkManager @Inject constructor(
  private val workManager: WorkManager,
  private val shellWorkDao: ShellWorkDao
) {

  companion object {
    private const val SHELL_WORK_TAG = "type:shell_work"
    private const val NAME_TAG_PREFIX = "name:"

    fun getName(tags: Set<String>) = tags.find { it.startsWith(NAME_TAG_PREFIX) }?.substring(NAME_TAG_PREFIX.length)
  }

  suspend fun list(): List<ShellWork> = shellWorkDao.findAll()

  suspend fun findByName(name: String) = shellWorkDao.findByName(name)

  private fun listWorkInfo(): ListenableFuture<MutableList<WorkInfo>> {
    return workManager.getWorkInfos(WorkQuery.fromTags(SHELL_WORK_TAG))
  }
  fun existsByName(name: String): Boolean {
    val works = listWorkInfo().get()
    return works.any { it.tags.contains("name:$name") }
  }

  suspend fun update(name: String, scriptText: String?): ShellWork? {
    if (scriptText != null) {
      shellWorkDao.updateScriptText(name, scriptText)
    }
    return shellWorkDao.findByName(name)
  }

  /**
   * Upsert a ShellWork (the ID being the name)
   */
  suspend fun save(
    name: String,
    description: String?,
    scriptText: String,
    period: WorkPeriod?,
    scheduleAt: LocalDateTime?,
    requiresNetwork: Boolean,
  ) {
    val operation = doWorkRequest(name, period, requiresNetwork)
    // waiting for the work to be created
    operation.result.get()

    val workId = listWorkInfo().get().find { it.tags.contains("name:$name") }!!.id
    // now create shell_work_data
    val data = ShellWork(
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
      startTime = null, endTime = null,
      logs = null, result = null, resultClassName = null, failedReason = null
    )
    shellWorkDao.upsert(data)
  }

  private fun doWorkRequest(
    name: String,
    period: WorkPeriod?,
    requiresNetwork: Boolean,
  ): Operation {
    val workRequest: WorkRequest.Builder<*, *> =
      if (period != null) PeriodicWorkRequestBuilder<MarshellWorkout>(period.toMinutes(), TimeUnit.MINUTES)
        .setInitialDelay(Duration.ZERO)
      else OneTimeWorkRequest.Builder(MarshellWorkout::class.java)
    workRequest.addTag(SHELL_WORK_TAG)
      // useful to check uniqueness without fetching shell_works, and to fetch work from workout
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

  suspend fun cancel(name: String): ShellWork? {
    workManager.cancelUniqueWork(name).result.get()
    shellWorkDao.updateState(name, WorkInfo.State.CANCELLED)
    return findByName(name)
  }

  suspend fun delete(name: String): Boolean {
    workManager.cancelUniqueWork(name).result.get()
    val data = shellWorkDao.findByName(name) ?: return false
    shellWorkDao.delete(data)
    return true
  }

  suspend fun runLateWorks() {
    val lateWorks = list().filter { work ->
      work.durationBetweenNowAndNext?.let { it.isNegative && it.abs().toMinutes() >= 10L } ?: false
    }
    lateWorks.forEach {
      val operation = doWorkRequest(it.name, it.period, it.isNetworkRequired)
      println(operation.result.get())
    }
  }
}