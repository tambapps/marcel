package com.tambapps.marcel.android.marshell.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.Routes.WORK_VIEW

// TODO delete this class
internal class WorkoutNotifier(
  private val applicationContext: Context,
) {
  private val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)


  companion object {
    const val NOTIFICATION_CHANNEL_ID = "MarcelShellWorker"
  }


  fun init() {
    createChannelIfNeeded()
  }

  private fun createChannelIfNeeded() {
    if (channelExists()) return
    val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Marshell Workout Notifications", NotificationManager.IMPORTANCE_DEFAULT)
    channel.description = "notifications for " + javaClass.name
    channel.enableLights(false)
    notificationManager.createNotificationChannel(channel)
  }

  private fun channelExists(): Boolean {
    val channel = notificationManager.getNotificationChannel(javaClass.name)
    return channel != null && channel.importance != NotificationManager.IMPORTANCE_NONE
  }

  private fun getConsultIntent(workName: String) {
    val notNestedIntent = TaskStackBuilder.create(applicationContext).run {
      addNextIntentWithParentStack(
        Intent(
          Intent.ACTION_VIEW,
          "app://marshell/${WORK_VIEW}/$workName".toUri() // <-- Notice this
        )
      )
      getPendingIntent(1234, PendingIntent.FLAG_UPDATE_CURRENT)
    }
  }
  /* TODO view work intent
  private fun getConsultIntent(notifId: Int, workName: String): PendingIntent? {
    val resultIntent = Intent(applicationContext, ShellWorkViewActivity::class.java)
    resultIntent.putExtra(ShellWorkViewFragment.SHELL_WORK_NAME_KEY, workName)
    resultIntent.putExtra(ShellWorkViewActivity.NOTIFICATION_ID_KEY, notifId)

    return TaskStackBuilder.create(applicationContext).run {
      // Add the intent, which inflates the back stack
      addNextIntentWithParentStack(resultIntent)
      // Get the PendingIntent containing the entire back stack
      getPendingIntent(0, PendingIntent.FLAG_MUTABLE)
    }

  }

   */
}