package com.tambapps.marcel.android.marshell.workout

import android.app.NotificationChannel
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import com.tambapps.marcel.android.marshell.os.WorkoutAndroidNotifier
import com.tambapps.marcel.android.marshell.repl.ShellSession
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkoutDao
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkout
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Named

@HiltWorker
class ShellWorkoutWorker @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val shellSessionFactory: ShellSessionFactory,
  private val shellWorkoutDao: ShellWorkoutDao,
  @Named("workoutNotificationChannel") private val workoutNotificationChannel: NotificationChannel
): CoroutineWorker(appContext, workerParams) {

  companion object {
    const val TAG = "MarshellWorkout"
  }

  override suspend fun doWork(): Result {
    val workout = findWork()
    if (workout == null) {
      Log.e(TAG, "Couldn't find shell_workout on database for workout with id $id and tags $tags")
      return Result.failure(Data.EMPTY)
    }
    val androidNotifier = WorkoutAndroidNotifier(applicationContext, workoutNotificationChannel, workout)
    if (workout.scriptText == null) {
      Log.e(TAG, "Couldn't retrieve script text of workout $id")
      return Result.failure(Data.EMPTY)
    }
    val printer = StringBuilderPrinter()
    val sessionResult = runCatching { shellSessionFactory.newWorkSession(printer, androidNotifier) }
    if (sessionResult.isFailure) {
      val e = sessionResult.exceptionOrNull()!!
      Log.e(TAG, "Couldn't start shell session", e)
      shellWorkoutDao.updateFailureReason(workout.name, e.message)
      return Result.failure(Data.EMPTY)
    }
    val shellSession = sessionResult.getOrThrow()
    try {
      return runWorkout(workout, workout.scriptText, shellSession, printer, androidNotifier)
    } finally {
      shellSession.classesDirectory.deleteRecursively()
    }
  }

  private suspend fun runWorkout(workout: ShellWorkout, scriptText: String, session: ShellSession, printer: StringBuilderPrinter, androidNotifier: WorkoutAndroidNotifier): Result {
    shellWorkoutDao.updateState(workout.name, WorkInfo.State.RUNNING)
    shellWorkoutDao.updateStartTime(workout.name, LocalDateTime.now())

    var result: marcel.util.Result<Any?> = marcel.util.Result.success(null)
    Log.d(TAG, "Evaluating init scripts")
    for (initScript in workout.initScripts ?: emptyList()) {
      result = result.then { session.eval(readInitScriptText(initScript)) }
    }
    Log.d(TAG, "Evaluating workout script")
    result = result.then { session.eval(scriptText) }

    shellWorkoutDao.update(
      name = workout.name,
      endTime = LocalDateTime.now(),
      result = result.getOrNull()?.toString(),
      resultClassName = result.getOrNull()?.javaClass?.name,
      failureReason = result.exceptionOrNull?.localizedMessage,
      logs = printer.toString(),
      state = if (workout.isPeriodic) WorkInfo.State.ENQUEUED
      else if (result.isSuccess) WorkInfo.State.SUCCEEDED
      else WorkInfo.State.FAILED
    )

    return if (result.isSuccess) {
      Log.d(TAG, "Finished successfully workout ${workout.name}. Return value: ${result.get()}")
      Result.success()
    } else {
      val e = result.exceptionOrNull
      Log.e(TAG, "An error occurred while executing script", e)
      androidNotifier.notifyIfEnabled("Workout ${workout.name} encountered an error", e?.message ?: "<no message>", onGoing = false)
      Result.failure()
    }
  }

  private suspend fun findWork(): ShellWorkout? {
    val name = ShellWorkoutManager.getName(tags)
    if (name == null) {
      Log.e(TAG, "Couldn't extract workout name from tags for workout with id $id and tags $tags")
      return null
    }
    var workout: ShellWorkout? = shellWorkoutDao.findByName(name)
    var tries = 1
    while (workout == null && tries++ < 4) {
      // sometimes it looks like the worker is created before the work_data could save the workout in database
      Thread.sleep(1_000L)
      workout = shellWorkoutDao.findByName(name)
    }
    if (workout != null && workout.workId != id) {
      workout = workout.copy(workId = id)
      shellWorkoutDao.update(workout)
    }
    return workout
  }

  private fun readInitScriptText(initScriptPath: String): String {
    val f = File(initScriptPath)
    if (!f.exists()) {
      throw IOException("File $initScriptPath doesn't exists")
    }
    if (!f.isFile) {
      throw IOException("File $initScriptPath is not a regular file")
    }
    return f.readText()
  }
}