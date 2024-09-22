package com.tambapps.marcel.android.marshell.os

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.tambapps.marcel.android.marshell.Routes
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkout

/**
 * Android Notifier for a workout, allowing to redirect to the work when clicking on the notification
 */
class WorkoutAndroidNotifier(context: Context, channel: NotificationChannel, private val workout: ShellWorkout) :
  AndroidNotifier(context, channel) {

  private val notificationId = workout.workId.hashCode()

  fun notifyIfEnabled(
    title: String,
    message: String,
    onGoing: Boolean,
  ) {
    if (!areNotificationEnabled) return
    notify(notificationId, title, message, onGoing, null)
  }

  override fun notify(
    notificationId: Int,
    title: String,
    message: String,
    onGoing: Boolean,
    contentIntent: PendingIntent?
  ) {
    // work notification should always be the same, same id
    super.notify(this.notificationId, title, message, onGoing,
      // we always want to redirect to the workout
      getConsultIntent())
  }

  private fun getConsultIntent(): PendingIntent? {
    val intent = Intent(
      Intent.ACTION_VIEW,
      "app://marshell/${Routes.WORKOUT_VIEW}/${workout.name}".toUri()
    )
    val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    else PendingIntent.FLAG_UPDATE_CURRENT
    return PendingIntent.getActivity(context, 1234, intent, flags)
  }

}