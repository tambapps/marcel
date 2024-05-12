package com.tambapps.marcel.android.marshell.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import com.tambapps.marcel.android.marshell.R
import java.util.UUID

class WorkoutNotification private constructor(
  private val notificationManager: NotificationManager,
  private val workout: MarshellWorkout,
  private val notificationId: Int
  ) {

  companion object {
    private const val NOTIFICATION_CHANNEL_ID = "MarcelShellWorker"

    fun newInstance(workout: MarshellWorkout, workId: UUID): WorkoutNotification? {
      val notificationManager = workout.applicationContext.getSystemService(NotificationManager::class.java)
      if (!notificationManager.areNotificationsEnabled()) {
        return null
      }
      createChannelIfNeeded(notificationManager)
      return WorkoutNotification(notificationManager, workout, workId.hashCode())
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

  private val context = workout.applicationContext
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
    notificationBuilder.setContentText(this.message)
    val notification = notificationBuilder.build()
    workout.setForegroundAsync(ForegroundInfo(notificationId, notification))
    notificationManager.notify(notificationId, notification)
  }
}