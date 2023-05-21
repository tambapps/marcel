package com.tambapps.marcel.android.marshell.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
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
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDataDao
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkData
import com.tambapps.marcel.android.marshell.ui.shellwork.form.PeriodUnit
import com.tambapps.marcel.android.marshell.work.MarcelShellWorker
import com.tambapps.marcel.android.marshell.work.ShellWork
import java.io.File
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class ShellWorkManager @Inject constructor(
  private val workManager: WorkManager,
  private val shellWorkDataDao: ShellWorkDataDao
  ) {

  companion object {
    const val SHELL_WORK_TAG = "type:shell_work"
  }

  suspend fun list(): List<ShellWork> {
    return shellWorkDataDao.findAll()
      .map { ShellWork.from(it) }
  }

  suspend fun listLive(): LiveData<List<ShellWork>> {
    val workDatas = shellWorkDataDao.findAll()
    return workManager.getWorkInfosByTagLiveData(SHELL_WORK_TAG)
      .map { _ ->
        // we just want to listen to changes but we actually just care of shellWorkDatas
        workDatas.map { ShellWork.from(it) }
      }
  }

  suspend fun findById(id: UUID): ShellWork? {
    val data = shellWorkDataDao.findById(id) ?: return null
    return ShellWork.from(data)
  }

  private fun listWorkInfo(): ListenableFuture<MutableList<WorkInfo>> {
    return workManager.getWorkInfos(WorkQuery.fromTags(SHELL_WORK_TAG))
  }
  fun existsByName(name: String): Boolean {
    val works = listWorkInfo().get()
    return works.any { it.tags.contains("name:$name") }
  }

  suspend fun create(periodAmount: Int?, periodUnit: PeriodUnit?, name: String, scriptFile: File,
                         description: String?, networkRequired: Boolean, silent: Boolean,
                         scheduleDate: LocalDate?, scheduleTime: LocalTime?) {
    val workRequest: WorkRequest.Builder<*, *> =
      if (periodAmount != null && periodUnit != null) PeriodicWorkRequestBuilder<MarcelShellWorker>(
        periodUnit.toMinutes(periodAmount), TimeUnit.MINUTES)
        .setInitialDelay(Duration.ZERO)
      else OneTimeWorkRequest.Builder(MarcelShellWorker::class.java)
    workRequest.addTag(SHELL_WORK_TAG)
        // useful to check uniqueness without fetching shell_work_data
      .addTag("name:$name")

    val scheduleDateTime =
      if (scheduleDate != null && scheduleTime != null) LocalDateTime.of(scheduleDate, scheduleTime) else null

    if (scheduleDate != null && scheduleTime != null) {
      workRequest.setInitialDelay(
        Duration.ofMillis(
        LocalDateTime.now().until(scheduleDateTime, ChronoUnit.MILLIS)))
    }
    if (networkRequired) {
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
    val data = ShellWorkData(
      id = workId,
      name = name,
      description = description,
      periodAmount = periodAmount,
      periodUnit = periodUnit,
      scheduledAt = scheduleDateTime?.toString(),
      silent = silent,
      scriptText = scriptFile.readText(),
      state = WorkInfo.State.ENQUEUED,
      startTime = null, endTime = null,
      logs = null, result = null, failedReason = null
    )
    shellWorkDataDao.insert(data)
  }

  suspend fun cancel(id: UUID) {
    workManager.cancelWorkById(id).result.get()
    shellWorkDataDao.updateState(id, WorkInfo.State.CANCELLED)
  }

  suspend fun delete(id: UUID): Boolean {
    workManager.cancelWorkById(id).result.get()
    val data = shellWorkDataDao.findById(id) ?: return false
    shellWorkDataDao.delete(data)
    return true
  }

}