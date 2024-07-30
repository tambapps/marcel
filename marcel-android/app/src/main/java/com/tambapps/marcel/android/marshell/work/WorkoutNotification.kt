package com.tambapps.marcel.android.marshell.work

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.tambapps.marcel.android.marshell.Routes
import com.tambapps.marcel.android.marshell.os.AndroidNotifier
import com.tambapps.marcel.android.marshell.room.entity.ShellWork

internal class WorkoutNotification constructor(
  private val context: Context,
  private val notifier: AndroidNotifier,
  private val work: ShellWork
  ) {

  private val notificationId = work.workId.hashCode()
  private var title: String = ""
  private var message: String = ""

  fun notify(
    title: String? = null,
    message: String? = null,
    onGoing: Boolean = true,
  ) {
    if (title != null) this.title = title
    if (message != null) this.message = message
    notifier.notify(notificationId, this.title, this.message, onGoing, if (onGoing) null else getConsultIntent())
  }

  private fun getConsultIntent(): PendingIntent? {
    val intent = Intent(
      Intent.ACTION_VIEW,
      "app://marshell/${Routes.WORK_VIEW}/${work.name}".toUri()
    )
    val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    else PendingIntent.FLAG_UPDATE_CURRENT
    return PendingIntent.getActivity(context, 1234, intent, flags)
  }
}