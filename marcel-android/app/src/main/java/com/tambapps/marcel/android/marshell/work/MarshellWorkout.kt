package com.tambapps.marcel.android.marshell.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import com.tambapps.marcel.android.marshell.os.AndroidNotifier
import com.tambapps.marcel.android.marshell.repl.ShellSession
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDao
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDateTime
import javax.inject.Named

@HiltWorker
class MarshellWorkout @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val shellSessionFactory: ShellSessionFactory,
  private val shellWorkDao: ShellWorkDao,
  @Named("workoutAndroidNotifier")
  internal val androidNotifier: AndroidNotifier
): CoroutineWorker(appContext, workerParams) {

  companion object {
    const val TAG = "MarshellWorkout"
  }

  private var notification: WorkoutNotification? = null

  override suspend fun doWork(): Result {
    val work = findWork()
    if (work == null) {
      Log.e(TAG, "Couldn't find shell_work on database for work with id $id and tags $tags")
      return Result.failure(Data.EMPTY)
    }
    if (!work.isSilent) {
      notification = if (androidNotifier.areNotificationEnabled) WorkoutNotification(applicationContext, androidNotifier, work).apply {
        notify(title = "Initializing Workout ${work.name}...")
      } else null
    }
    if (work.scriptText == null) {
      Log.e(TAG, "Couldn't retrieve script text of work $id")
      return Result.failure(Data.EMPTY)
    }
    val printer = StringBuilderPrinter()
    val sessionResult = runCatching { shellSessionFactory.newWorkSession(printer) }
    if (sessionResult.isFailure) {
      val e = sessionResult.exceptionOrNull()!!
      Log.e(TAG, "Couldn't start shell session", e)
      shellWorkDao.updateFailureReason(work.name, e.message)
      return Result.failure(Data.EMPTY)
    }
    val shellSession = sessionResult.getOrThrow()
    try {
      return runWorkout(work, work.scriptText, shellSession, printer)
    } finally {
      shellSession.classesDirectory.deleteRecursively()
    }
  }

  private suspend fun runWorkout(work: ShellWork, scriptText: String, session: ShellSession, printer: StringBuilderPrinter): Result {
    notification?.notify(title = "Running workout ${work.name}...")
    shellWorkDao.updateState(work.name, WorkInfo.State.RUNNING)
    shellWorkDao.updateStartTime(work.name, LocalDateTime.now())

    val result = runCatching { session.eval(scriptText) }

    shellWorkDao.update(
      name = work.name,
      endTime = LocalDateTime.now(),
      result = result.getOrNull()?.toString(),
      resultClassName = result.getOrNull()?.javaClass?.name,
      failureReason = result.exceptionOrNull()?.localizedMessage,
      logs = printer.toString(),
      state = if (work.isPeriodic) WorkInfo.State.ENQUEUED
      else if (result.isSuccess) WorkInfo.State.SUCCEEDED
      else WorkInfo.State.FAILED
    )

    return if (result.isSuccess) {
      Log.d(TAG, "Finished successfully workout ${work.name}. Return value: ${result.getOrNull()}")
      notification?.notify(title = "Workout ${work.name} ran successfully", onGoing = false)
      Result.success()
    } else {
      Log.e(TAG, "An error occurred while executing script", result.exceptionOrNull())
      notification?.notify(title = "Workout ${work.name} encountered an error", onGoing = false)
      Result.failure()
    }
  }

  private suspend fun findWork(): ShellWork? {
    val name = ShellWorkManager.getName(tags)
    if (name == null) {
      Log.e(TAG, "Couldn't extract work name from tags for work with id $id and tags $tags")
      return null
    }
    var work: ShellWork? = shellWorkDao.findByName(name)
    var tries = 1
    while (work == null && tries++ < 4) {
      // sometimes it looks like the worker is created before the work_data could save the work in database
      Thread.sleep(1_000L)
      work = shellWorkDao.findByName(name)
    }
    if (work != null && work.workId != id) {
      work = work.copy(workId = id)
      shellWorkDao.update(work)
    }
    return work
  }
}