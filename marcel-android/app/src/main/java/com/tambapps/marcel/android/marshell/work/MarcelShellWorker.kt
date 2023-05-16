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
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tambapps.marcel.android.marshell.MainActivity
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.repl.jar.DexJarWriterFactory
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDataDao
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.MarcelEvaluator
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplJavaTypeResolver
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.time.LocalDateTime

@HiltWorker
class MarcelShellWorker
  @AssistedInject constructor(@Assisted appContext: Context,
                              @Assisted workerParams: WorkerParameters,
                              // this is not a val because hilt doesn't allow final fields when injecting
                              private val compilerConfiguration: CompilerConfiguration,
                              private val shellWorkDataDao: ShellWorkDataDao):
  CoroutineWorker(appContext, workerParams) {

  companion object {
    const val NOTIFICATION_CHANNEL_ID = "MarcelShellWorker"
  }
  private val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
  private var notificationTitle = "Marshell Worker"
  private var isSilent = false

  override suspend fun doWork(): Result {
    val work = shellWorkDataDao.findById(id)
    if (work == null) {
      Log.e("MarcelShellWorker", "Couldn't find work on database")
      notificationTitle = "Marshell Worker"
      return Result.failure(endData(failedReason = "Work misconfigured"))
    }
    isSilent = work.silent
    notificationTitle = work.name + " Marshell Worker"

    /* initialization */
    createChannelIfNeeded()
    shellWorkDataDao.updateStartTime(id, LocalDateTime.now())
    val binding = Binding() // TODO set a variable 'out' to allow printing to a file without any conflicts
    val classLoader = MarcelDexClassLoader()
    val typeResolver = ReplJavaTypeResolver(classLoader, binding)
    val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, typeResolver)
    val directory = File(applicationContext.getDir("shell_works", Context.MODE_PRIVATE), "work $id")
    notification(content = "Initializing marshell work...")

    val text = try {
      File(work.scriptFilePath).readText()
    } catch (e: IOException) {
      notification(content = "Couldn't read script: ${e.message}", foregroundNotification = true)
      return Result.failure(endData(failedReason = e.message))
    }

    if (!directory.mkdir()) {
      val failedReason = "Couldn't create worker directory"
      notification(content = failedReason, foregroundNotification = true)
      return Result.failure(endData(failedReason = failedReason))
    }

    /* running */
    val evaluator = MarcelEvaluator(Binding(), replCompiler, classLoader, DexJarWriterFactory(), directory)
    notification(content = "Executing Marshell work...")
    try {
      val result = evaluator.eval(text)
      /* handling result */
      val contentBuilder = StringBuilder("Work finished successfully")
      if (result != null) {
        shellWorkDataDao.updateResult(id, result.toString())
        contentBuilder.append("\nResult: $result")
      }

      notification(content = contentBuilder.toString(), foregroundNotification = true)
      return Result.success(endData())
    } catch (e: Exception) {
      notification(content = "Error while executing script: ${e.message}", foregroundNotification = true)
      return Result.failure(endData(failedReason = e.message))
    } finally {
      directory.deleteRecursively()
    }
  }

  private suspend fun endData(failedReason: String? = null): Data {
    shellWorkDataDao.updateEndTime(id, LocalDateTime.now())
    if (failedReason != null) {
      shellWorkDataDao.updateFailureReason(id, failedReason)
    }
    return Data.Builder().build()
  }


  /* Notification stuff */
  private fun notification(title: String = notificationTitle, content: String, foregroundNotification: Boolean = false) {
    if (isSilent) return
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
    } else {
      notificationBuilder.addAction(android.R.drawable.btn_plus, "consult", getConsultIntent(notifId, "TODO"))
    }

    val notification = notificationBuilder.build()

    if (foregroundNotification) {
      notificationManager.notify(notifId, notification)
    } else {
      setForegroundAsync(ForegroundInfo(notifId, notification))
    }
  }

  private fun getConsultIntent(notifId: Int, workName: String): PendingIntent? {
    val resultIntent = Intent(applicationContext, MainActivity::class.java)
    /* TODO put args to redirect to shell work page
    resultIntent.putExtra(WorkFragment.WORK_NAME_KEY, workName)
    resultIntent.putExtra(NOTIFICATION_ID_KEY, notifId)

     */
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
}