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
import androidx.work.WorkManager
import androidx.work.WorkRequest
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
  private val shellWorkDataDao: ShellWorkDataDao,
  @Named("shellWorksDirectory")
  private val shellWorksDirectory: File
  ) {

  suspend fun list(): LiveData<List<ShellWork>> {
    val worksDataById = shellWorkDataDao.findAll()
      .associateBy { it.id }
    return workManager.getWorkInfosByTagLiveData("type:" + ShellWork.SHELL_WORK_TYPE)
      .map { workInfos ->
        workInfos.mapNotNull {
          val data = worksDataById[it.id] ?: return@mapNotNull null
          return@mapNotNull ShellWork.from(it, data)
        }
      }
  }

  suspend fun findById(id: UUID): ShellWork? {
    val info = workManager.getWorkInfoById(id).get()
    val data = shellWorkDataDao.findById(id) ?: return null
    return ShellWork.from(info, data)
  }

  suspend fun create(periodAmount: Int?, periodUnit: PeriodUnit?, name: String, scriptFile: File,
                         description: String?, networkRequired: Boolean, silent: Boolean,
                         scheduleDate: LocalDate?, scheduleTime: LocalTime?) {
    val workRequest: WorkRequest.Builder<*, *> =
      if (periodAmount != null && periodUnit != null) PeriodicWorkRequestBuilder<MarcelShellWorker>(
        periodUnit.toMinutes(periodAmount), TimeUnit.MINUTES)
      else OneTimeWorkRequest.Builder(MarcelShellWorker::class.java)

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
    val id = UUID.randomUUID() // not optimal but this is how android work-api also creates it anyway
    workRequest.setId(id)

    val operation = if (workRequest is PeriodicWorkRequest.Builder) workManager.enqueueUniquePeriodicWork(name, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, workRequest.build())
    else workManager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, workRequest.build() as OneTimeWorkRequest)
    // waiting for the work to be created
    operation.result.get()

    // now create work directory
    val workDirectory = workDirectory(id)
    workDirectory.mkdir()
    val workScriptFile = workInfoFile(id)
    scriptFile.copyTo(workScriptFile, overwrite = true)

    // now create shell_work_data
    val data = ShellWorkData(
      id = id,
      name = name,
      description = description,
      periodAmount = periodAmount,
      periodUnit = periodUnit,
      scheduledAt = scheduleDateTime?.toString(),
      silent = silent,
      scriptFilePath = workScriptFile.absolutePath,
      startTime = null, endTime = null,
      output = null, result = null, failedReason = null
    )
    shellWorkDataDao.insert(data)
  }

  fun cancel(id: UUID) {
    workManager.cancelWorkById(id).result.get()
  }

  suspend fun delete(id: UUID): Boolean {
    workManager.cancelWorkById(id).result.get()
    val data = shellWorkDataDao.findById(id) ?: return false
    shellWorkDataDao.delete(data)
    return true
  }

  private fun workDirectory(id: UUID): File {
    return File(shellWorksDirectory, "work_$id")
  }

  private fun workInfoFile(id: UUID): File {
    return File(workDirectory(id), "info.parcelable")
  }
}