package com.tambapps.marcel.android.marshell.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import com.tambapps.marcel.android.marshell.repl.MarshellScript
import com.tambapps.marcel.android.marshell.repl.jar.DexJarWriterFactory
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDao
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.compiler.CompilerConfiguration
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
  private val compilerConfiguration: CompilerConfiguration,
  private val shellWorkDao: ShellWorkDao,
  @Named("shellWorksDirectory")
  private val shellWorksDirectory: File
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
    val workDirectory = File(shellWorksDirectory, "work_$id")
    if (!workDirectory.isDirectory && !workDirectory.mkdirs()) {
      Log.e(TAG, "Couldn't create workout directory of work $id")
      //notification(content = "An unexpected work configuration error occurred", force = true)
      return Result.failure(Data.EMPTY)
    }
    val classesDirectory = File(workDirectory, "classes")
    if (!classesDirectory.isDirectory && !classesDirectory.mkdirs()) {
      Log.e(TAG, "Couldn't create workout directory of work $id")
      //notification(content = "An unexpected work configuration error occurred", force = true)
      return Result.failure(Data.EMPTY)
    }
    try {
      runWorkout(work, work.scriptText, classesDirectory)
    } finally {
      workDirectory.deleteRecursively()
    }
    return Result.success()
  }

  private suspend fun runWorkout(work: ShellWork, scriptText: String, classesDirectory: File): Result {
    val binding = Binding()
    val classLoader = MarcelDexClassLoader()
    val symbolResolver = ReplMarcelSymbolResolver(classLoader, binding)
    val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, symbolResolver)
    val evaluator = MarcelEvaluator(binding, replCompiler, classLoader, DexJarWriterFactory(), classesDirectory)
    val printer = StringBuilderPrinter()
    evaluator.scriptConfigurer = { script ->
      (script as MarshellScript).setPrinter(printer)
    }
    //notification(content = "Executing Marshell work...")
    shellWorkDao.updateState(work.name, WorkInfo.State.RUNNING)

    shellWorkDao.updateStartTime(work.name, LocalDateTime.now())
    try {
      val result = evaluator.eval(scriptText)
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
    } catch (e: Exception) {
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