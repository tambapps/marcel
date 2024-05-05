package com.tambapps.marcel.android.marshell.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
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
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriodUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShellWorkManager @Inject constructor(
  @ApplicationContext private val applicationContext: Context,
  private val shellWorkDao: ShellWorkDao
) {

  private companion object {
    const val SHELL_WORK_TAG = "type:shell_work"
  }

  private val workManager by lazy { WorkManager.getInstance(applicationContext) }

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

  suspend fun listFake(): List<ShellWork> {
    return listOf(
      ShellWork(
      name = "Work 1",
      workId = UUID.randomUUID(),
        description = "some work",
        isNetworkRequired = true,
        isSilent = false,
        state = WorkInfo.State.ENQUEUED,
        period = WorkPeriod(amount = 2, unit = WorkPeriodUnit.DAYS),
        startTime = LocalDateTime.now().minusSeconds(36000),
        endTime = LocalDateTime.now().minusSeconds(26000),
        scheduledAt = null,
        logs = null,
        result = null,
        failedReason = null,
        scriptText = null
    )
    )
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
    silent: Boolean
  ) {
    val workRequest: WorkRequest.Builder<*, *> =
      if (period != null) PeriodicWorkRequestBuilder<MarshellWorkout>(period.toMinutes(), TimeUnit.MINUTES)
        .setInitialDelay(Duration.ZERO)
      else OneTimeWorkRequest.Builder(MarshellWorkout::class.java)
    workRequest.addTag(SHELL_WORK_TAG)
      // useful to check uniqueness without fetching shell_work_data
      .addTag("name:$name")

    if (requiresNetwork) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
      workRequest.setConstraints(constraints)
    }
    workRequest.setInputData(Data.Builder().build())

    val operation = if (workRequest is PeriodicWorkRequest.Builder) workManager.enqueueUniquePeriodicWork(name, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, workRequest.build())
    else workManager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, workRequest.build() as OneTimeWorkRequest)
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
      isSilent = silent,
      scriptText = scriptText,
      state = WorkInfo.State.ENQUEUED,
      startTime = null, endTime = null,
      logs = null, result = null, failedReason = null
    )
    shellWorkDao.upsert(data)
  }
}