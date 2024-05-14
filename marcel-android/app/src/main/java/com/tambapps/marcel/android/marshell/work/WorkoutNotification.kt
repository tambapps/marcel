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
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.Routes
import com.tambapps.marcel.android.marshell.room.entity.ShellWork

class WorkoutNotification private constructor(
  private val context: Context,
  private val notificationManager: NotificationManager,
  private val work: ShellWork
  ) {

  private val notificationId = work.workId.hashCode()

  companion object {
    private const val NOTIFICATION_CHANNEL_ID = "MarcelShellWorker"

    fun newInstance(workout: MarshellWorkout, woek: ShellWork): WorkoutNotification? {
      val notificationManager = workout.applicationContext.getSystemService(NotificationManager::class.java)
      if (!notificationManager.areNotificationsEnabled()) {
        return null
      }
      createChannelIfNeeded(notificationManager)
      return WorkoutNotification(workout.applicationContext, notificationManager, woek)
    }

    private fun createChannelIfNeeded(notificationManager: NotificationManager) {
      if (channelExists(notificationManager)) return
      val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Marshell Workout Notifications", NotificationManager.IMPORTANCE_DEFAULT)
      channel.description = "Shell Workouts notifications"
      channel.enableLights(false)
      notificationManager.createNotificationChannel(channel)
    }

    private fun channelExists(notificationManager: NotificationManager): Boolean {
      val channel = notificationManager.getNotificationChannel(MarshellWorkout::javaClass.name)
      return channel != null && channel.importance != NotificationManager.IMPORTANCE_NONE
    }
  }

  private var title: String = ""
  private var message: String = ""

  fun notify(
    title: String? = null,
    message: String? = null,
    onGoing: Boolean = true,
  ) {
    if (title != null) this.title = title
    if (message != null) this.message = message

    val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
      .setContentTitle(this.title)
      .setTicker(this.title)
      .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.appicon))
      .setSmallIcon(R.drawable.prompt)
      .setOngoing(onGoing)
      .setOnlyAlertOnce(true)
    notificationBuilder.setContentText(this.message)
    if (!onGoing) {
      notificationBuilder.setContentIntent(getConsultIntent())
    }
    val notification = notificationBuilder.build()
    notificationManager.notify(notificationId, notification)
  }

  private fun getConsultIntent(): PendingIntent? {
    return TaskStackBuilder.create(context).run {
      // TODO test this
      addNextIntentWithParentStack(
        Intent(
          Intent.ACTION_VIEW,
          "app://marshell/${Routes.WORK_VIEW}/${work.name}".toUri() // <-- Notice this
        )
      )
      val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      else PendingIntent.FLAG_UPDATE_CURRENT

      getPendingIntent(1234, flags)
    }
  }
}