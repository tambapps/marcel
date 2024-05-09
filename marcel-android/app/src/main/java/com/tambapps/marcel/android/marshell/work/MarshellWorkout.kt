package com.tambapps.marcel.android.marshell.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import com.tambapps.marcel.android.marshell.repl.MarshellScript
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.repl.jar.DexJarWriterFactory
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDao
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.dumbbell.DumbbellEngine
import com.tambapps.marcel.repl.MarcelEvaluator
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import java.io.File
import java.lang.Exception
import java.time.LocalDateTime
import javax.inject.Named

@HiltWorker
class MarshellWorkout @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val shellSessionFactory: ShellSessionFactory,
  private val shellWorkDao: ShellWorkDao,
  @Named("shellWorksDirectory")
  private val shellWorksDirectory: File,
  private val dumbbellEngine: DumbbellEngine
): CoroutineWorker(appContext, workerParams) {

  companion object {
    const val TAG = "MarshellWorkout"
  }

  private val notifier = WorkoutNotifier(applicationContext)

  override suspend fun doWork(): Result {
    notifier.init()
    val work = findWork()
    if (work == null) {
      Log.e(TAG, "Couldn't find work_data on database for work $id")
      //notification(content = "An unexpected work configuration error occurred", force = true)
      return Result.failure(Data.EMPTY)
    }
    if (work.scriptText == null) {
      Log.e(TAG, "Couldn't retrieve script text of work $id")
      //notification(content = "An unexpected work configuration error occurred", force = true)
      return Result.failure(Data.EMPTY)
    }
    runWorkout(work, work.scriptText)
    return Result.success()
  }

  private suspend fun runWorkout(work: ShellWork, scriptText: String): Result {
    // setup
    val printer = StringBuilderPrinter()
    val session = shellSessionFactory.newSession(printer) // TODO try/catch this
    Dumbbell.setEngine(dumbbellEngine)

    //notification(content = "Executing Marshell work...")
    shellWorkDao.updateState(work.name, WorkInfo.State.RUNNING)

    shellWorkDao.updateStartTime(work.name, LocalDateTime.now())
    try {
      val result = session.eval(scriptText)
      shellWorkDao.updateEndTime(work.name, LocalDateTime.now())
      /* handling result */
      val contentBuilder = StringBuilder("Work finished successfully")
      if (result != null) {
        shellWorkDao.updateResult(work.name, result.toString())
        contentBuilder.append("\nResult: $result")
      }
      //notification(content = contentBuilder.toString(), foregroundNotification = true)
      shellWorkDao.updateState(work.name, if (work.isPeriodic) WorkInfo.State.ENQUEUED else WorkInfo.State.SUCCEEDED)
      return Result.success()
    } catch (e: Throwable) {
      shellWorkDao.updateEndTime(work.name, LocalDateTime.now())
      Log.e(TAG, "An error occurred while executing script", e)
      //notification(content = "Error while executing script: ${e.message}", foregroundNotification = true, force = true)
      shellWorkDao.updateState(work.name, if (work.isPeriodic) WorkInfo.State.ENQUEUED else WorkInfo.State.FAILED)
      shellWorkDao.updateFailureReason(work.name, "${e.javaClass.simpleName}: ${e.localizedMessage}")
      return Result.failure()
    } finally {
      shellWorkDao.updateLogs(work.name, printer.toString())
    }
  }

  private suspend fun findWork(): ShellWork? {
    var work = shellWorkDao.findById(id)
    var tries = 1
    while (work == null && tries++ < 4) {
      // sometimes it looks like the worker is created before the work_data could save the work in database
      Thread.sleep(1_000L)
      work = shellWorkDao.findById(id)
    }
    return work
  }
}