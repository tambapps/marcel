package com.tambapps.marcel.android.marshell.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.ShellWorkViewActivity
import com.tambapps.marcel.android.marshell.repl.jar.DexJarWriterFactory
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDao
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.ui.shellwork.view.ShellWorkViewFragment
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.dumbbell.DumbbellEngine
import com.tambapps.marcel.repl.MarcelEvaluator
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplJavaTypeResolver
import com.tambapps.maven.dependency.resolver.repository.RemoteSavingMavenRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import marcel.lang.printer.Printer
import java.io.File
import java.lang.Exception
import java.time.LocalDateTime

@HiltWorker
class MarcelShellWorker
  @AssistedInject constructor(@Assisted appContext: Context,
                              @Assisted workerParams: WorkerParameters,
                              // this is not a val because hilt doesn't allow final fields when injecting
                              private val compilerConfiguration: CompilerConfiguration,
                              private val dumbbellMavenRepository: RemoteSavingMavenRepository,
                              private val shellWorkDao: ShellWorkDao):
  CoroutineWorker(appContext, workerParams) {

  companion object {
    const val NOTIFICATION_CHANNEL_ID = "MarcelShellWorker"
  }
  private val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
  private var notificationTitle = "Shell Work"
  private var work: ShellWork? = null
  private val isSilent get() = work?.isSilent ?: false
  private val out = ShellWorkerPrinter()

  override suspend fun doWork(): Result {
    val work = findWork()
    if (work == null) {
      Log.e("MarcelShellWorker", "Couldn't find work_data on database for work $id")
      notificationTitle = "Marshell Worker"
      notification(content = "An unexpected work configuration error occurred", force = true)
      return Result.failure(endData(failedReason = "Unexpected work configuration error"))
    }
    this.work = work
    notificationTitle = work.name + " " + notificationTitle

    /* initialization */
    createChannelIfNeeded()
    notification(content = "Initializing marshell work...")
    // to ensure we use the right maven repository
    Dumbbell.setEngineUsingRepository(dumbbellMavenRepository)
    val binding = Binding()
    val classLoader = MarcelDexClassLoader()
    val typeResolver = ReplJavaTypeResolver(classLoader, binding)
    val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, typeResolver)
    val directory = File(applicationContext.getDir("shell_works", Context.MODE_PRIVATE), "work $id")

    typeResolver.setScriptVariable("out", out, Printer::class.java)

    val text = if (work.scriptText != null) {
      work.scriptText
    } else {
      notification(content = "Couldn't read script", foregroundNotification = true, force = true)
      return Result.failure(endData(failedReason = "Couldn't read script"))
    }

    if (!directory.mkdir()) {
      val failedReason = "Couldn't create worker directory"
      notification(content = failedReason, foregroundNotification = true, force = true)
      return Result.failure(endData(failedReason = failedReason))
    }

    /* running */
    val evaluator = MarcelEvaluator(binding, replCompiler, classLoader, DexJarWriterFactory(), directory)
    notification(content = "Executing Marshell work...")
    shellWorkDao.updateStartTime(work.name, LocalDateTime.now())
    try {
      val result = evaluator.eval(text)
      /* handling result */
      val contentBuilder = StringBuilder("Work finished successfully")
      if (result != null) {
        shellWorkDao.updateResult(work.name, result.toString())
        contentBuilder.append("\nResult: $result")
      }

      notification(content = contentBuilder.toString(), foregroundNotification = true)
      return Result.success(endData())
    } catch (e: Exception) {
      Log.e("MarcelShellWorker", "An error occurred while executing script", e)
      notification(content = "Error while executing script: ${e.message}", foregroundNotification = true, force = true)
      return Result.failure(endData(failedReason = e.message))
    } finally {
      directory.deleteRecursively()
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

  // this is basically just a callback to update data at the end of a shell work
  private suspend fun endData(failedReason: String? = null): Data {
    val workName = work?.name ?: return Data.Builder().build()
    shellWorkDao.updateEndTime(workName, LocalDateTime.now())
    shellWorkDao.updateFailureReason(workName, failedReason)
    if (failedReason != null) {
      shellWorkDao.updateState(workName, if (work?.isPeriodic == true) WorkInfo.State.ENQUEUED else WorkInfo.State.FAILED)
      shellWorkDao.updateResult(workName, null)
    } else {
      shellWorkDao.updateState(workName, if (work?.isPeriodic == true) WorkInfo.State.ENQUEUED else WorkInfo.State.SUCCEEDED)
    }
    val logs = out.logs.trim()
    if (logs.isNotEmpty()) {
      shellWorkDao.updateLogs(workName, logs)
    }
    return Data.Builder().build()
  }

  /* Notification stuff */
  private fun notification(title: String = notificationTitle, content: String, foregroundNotification: Boolean = false, force: Boolean = false) {
    if (!force && isSilent) return
    val notifId = if (foregroundNotification) id.hashCode() + 1 else id.hashCode()
    val notificationBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
      .setContentTitle(title)
      .setTicker(title)
      .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.home))
      .setSmallIcon(R.drawable.shell)
      .setOngoing(!foregroundNotification)
    if (content.count { it == '\n' } >= 2) {
      notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(content))
    } else {
      notificationBuilder.setContentText(content)
    }
    if (!foregroundNotification) {
      notificationBuilder.addAction(android.R.drawable.ic_delete, "cancel",
        WorkManager.getInstance(applicationContext)
          .createCancelPendingIntent(id))
    }
    work?.name?.let { workName -> notificationBuilder.setContentIntent(getConsultIntent(notifId, workName)) }

    val notification = notificationBuilder.build()

    if (foregroundNotification) {
      notificationManager.notify(notifId, notification)
    } else {
      setForegroundAsync(ForegroundInfo(notifId, notification))
    }
  }

  private fun getConsultIntent(notifId: Int, workName: String): PendingIntent? {
    val resultIntent = Intent(applicationContext, ShellWorkViewActivity::class.java)
    resultIntent.putExtra(ShellWorkViewFragment.SHELL_WORK_NAME_KEY, workName)
    resultIntent.putExtra(ShellWorkViewActivity.NOTIFICATION_ID_KEY, notifId)
    println("cacacacac ${resultIntent?.extras}")

    return TaskStackBuilder.create(applicationContext).run {
      // Add the intent, which inflates the back stack
      addNextIntentWithParentStack(resultIntent)
      // Get the PendingIntent containing the entire back stack
      getPendingIntent(0, PendingIntent.FLAG_MUTABLE)
    }

  }

  private fun createChannelIfNeeded() {
    if (isSilent || channelExists()) return
    val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Shell Work Notifications", NotificationManager.IMPORTANCE_DEFAULT)
    channel.description = "notifications for " + javaClass.name
    channel.enableLights(false)
    notificationManager.createNotificationChannel(channel)
  }

  private fun channelExists(): Boolean {
    val channel = notificationManager.getNotificationChannel(javaClass.name)
    return channel != null && channel.importance != NotificationManager.IMPORTANCE_NONE
  }

  private class ShellWorkerPrinter: Printer {
    // using StringBuffer because it is thread-safe (synchronized), as opposed to StringBuilder
    val builder = StringBuffer()

    val logs get() = builder.toString()
    override fun print(p0: CharSequence?) {
      builder.append(p0)
    }

    override fun println(p0: CharSequence?) {
      builder.append(p0)
        .append("\n")
    }

    override fun println() {
      builder.append("\n")
    }

    override fun toString(): String {
      return "Shell Work Printer"
    }
  }
}