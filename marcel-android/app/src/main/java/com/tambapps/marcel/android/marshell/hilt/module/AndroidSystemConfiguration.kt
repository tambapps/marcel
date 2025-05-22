package com.tambapps.marcel.android.marshell.hilt.module

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.telephony.SmsManager
import com.tambapps.marcel.android.marshell.os.AndroidNotifier
import com.tambapps.marcel.android.marshell.os.AndroidSmsSender
import com.tambapps.marcel.android.marshell.os.AndroidSystemImpl
import com.tambapps.marcel.android.marshell.room.dao.MessageDao
import com.tambapps.marcel.android.marshell.service.PermissionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import marcel.lang.android.AndroidSystemHandler
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class AndroidSystemConfiguration {

  @Provides
  fun notificationManager(@ApplicationContext context: Context): NotificationManager = context.getSystemService(
    NotificationManager::class.java)

  @Named("workoutNotificationChannel")
  @Provides
  fun workoutNotificationChannel(notificationManager: NotificationManager) = createOrGetChannel(
    notificationManager,
    id = "MarcelShellWorker",
    name = "Marshell Workout Notifications",
    description = "Shell Workouts notifications"
  )

  @Named("marcelMessageNotificationChannel")
  @Provides
  fun marcelMessageNotificationChannel(notificationManager: NotificationManager) = createOrGetChannel(
    notificationManager,
    id = "MarcelMessage",
    name = "Marshell Message Notifications",
    description = "Messages sent from Marcel scripts"
  )

  @Named("shellNotificationChannel")
  @Provides
  fun shellNotificationChannel(notificationManager: NotificationManager) = createOrGetChannel(
    notificationManager,
    id = "MarshellSession",
    name = "Marshell Notifications",
    description = "Shell session notifications"
  )

  @Provides
  fun androidNotifier(@ApplicationContext context: Context,
                      notificationManager: NotificationManager,
                      @Named("marcelMessageNotificationChannel") marcelMessageNotificationChannel: NotificationChannel): AndroidNotifier {
    return AndroidNotifier(context, notificationManager, marcelMessageNotificationChannel)
  }

  @Provides
  fun smsManager(@ApplicationContext context: Context): SmsManager = context.getSystemService(SmsManager::class.java)

  @Provides
  fun smsSender(@ApplicationContext context: Context, smsManager: SmsManager, messageDao: MessageDao): AndroidSmsSender = AndroidSmsSender(context, smsManager, messageDao)

  @Provides
  fun androidSystem(
    androidNotifier: AndroidNotifier,
    smsSender: AndroidSmsSender,
    permissionManager: PermissionManager
  ): AndroidSystemHandler {
    return AndroidSystemImpl(androidNotifier, smsSender, permissionManager)
  }

  private fun createOrGetChannel(
    notificationManager: NotificationManager,
    id: String,
    name: String,
    description: String
  ): NotificationChannel {
    val existingChannel = notificationManager.getNotificationChannel(id)
    if (existingChannel != null && existingChannel.importance != NotificationManager.IMPORTANCE_NONE) return existingChannel
    val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
    channel.description = description
    channel.enableLights(false)
    notificationManager.createNotificationChannel(channel)
    return notificationManager.getNotificationChannel(id)!!
  }
}