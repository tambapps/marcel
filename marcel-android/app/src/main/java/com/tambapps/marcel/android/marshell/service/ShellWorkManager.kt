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
import com.tambapps.marcel.android.marshell.ui.shellwork.form.PeriodUnit
import com.tambapps.marcel.android.marshell.work.MarcelShellWorkInfo
import com.tambapps.marcel.android.marshell.work.MarcelShellWorker
import com.tambapps.marcel.android.marshell.work.WorkTags
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
  @Named("shellWorksDirectory")
  private val shellWorksDirectory: File
  ) {


  fun listWorks(): LiveData<MarcelShellWorkInfo> {
    TODO()
  }

  fun create(periodAmount: Long?, periodUnit: PeriodUnit?, name: String, scriptFile: File,
                         description: String?, networkRequired: Boolean, silent: Boolean,
                         scheduleDate: LocalDate?, scheduleTime: LocalTime?) {
    val workRequest: WorkRequest.Builder<*, *> =
      if (periodAmount != null && periodUnit != null) PeriodicWorkRequestBuilder<MarcelShellWorker>(
        periodUnit.toMinutes(periodAmount), TimeUnit.MINUTES)
        .addTag(WorkTags.periodAmount(periodAmount))
        .addTag(WorkTags.periodUnit(periodUnit))
      else OneTimeWorkRequest.Builder(MarcelShellWorker::class.java)

    if (scheduleDate != null && scheduleTime != null) {
      val scheduleDateTime = LocalDateTime.of(scheduleDate, scheduleTime)
      workRequest.setInitialDelay(
        Duration.ofMillis(
        LocalDateTime.now().until(scheduleDateTime, ChronoUnit.MILLIS)))
        .addTag(WorkTags.schedule(scheduleDateTime.toString()))
    }
    if (networkRequired) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
      workRequest.setConstraints(constraints)
    }

    workRequest.apply {
      addTag(WorkTags.type(WorkTags.SHELL_WORK_TYPE))
      addTag(WorkTags.name(name))
      addTag(WorkTags.silent(silent))
      addTag(WorkTags.networkRequired(networkRequired))
      if (!description.isNullOrBlank()) {
        addTag(WorkTags.description(description))
      }
      addTag(WorkTags.scriptPath(scriptFile.absolutePath))
      setInputData(Data.Builder().build())
    }
    val id = UUID.randomUUID() // not optimal but this is how android work-api also creates it anyway
    workRequest.setId(id)

    val operation = if (workRequest is PeriodicWorkRequest.Builder) workManager.enqueueUniquePeriodicWork(name, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, workRequest.build())
    else workManager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, workRequest.build() as OneTimeWorkRequest)
    // waiting for the work to be created
    operation.result.get()

    // now create work directory
    val workDirectory = workDirectory(id)
    workDirectory.mkdir()

    val workInfoFile = workInfoFile(id)
    // TODO
    TODO()

  }


  private fun workDirectory(id: UUID): File {
    return File(shellWorksDirectory, "work_$id")
  }


  private fun workInfoFile(id: UUID): File {
    return File(workDirectory(id), "info.parcelable")
  }
}