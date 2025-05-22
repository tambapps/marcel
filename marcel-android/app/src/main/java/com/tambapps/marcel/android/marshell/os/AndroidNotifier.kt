package com.tambapps.marcel.android.marshell.os

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.tambapps.marcel.android.marshell.R

class AndroidNotifier constructor(
  private val context: Context,
  private val notificationManager: NotificationManager,
  private val channel: NotificationChannel
) {

  val areNotificationEnabled: Boolean get() = notificationManager.areNotificationsEnabled()

  open fun notify(
    notificationId: Int,
    title: String,
    message: String,
    onGoing: Boolean,
    contentIntent: PendingIntent? = null
  ) {
    val notificationBuilder = NotificationCompat.Builder(context, channel.id)
      .setContentTitle(title)
      .setTicker(message)
      .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.appicon))
      .setSmallIcon(R.drawable.prompt)
      .setOngoing(onGoing)
      .setOnlyAlertOnce(true)
    notificationBuilder.setContentText(message)
    if (contentIntent != null) {
      notificationBuilder.setContentIntent(contentIntent)
        .setAutoCancel(true)
    }
    val notification = notificationBuilder.build()
    notificationManager.notify(notificationId, notification)
  }
}