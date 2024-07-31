package com.tambapps.marcel.android.marshell.hilt.module

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.telephony.SmsManager
import com.tambapps.marcel.android.marshell.os.AndroidNotifier
import com.tambapps.marcel.android.marshell.os.AndroidSmsSender
import com.tambapps.marcel.android.marshell.os.AndroidSystemImpl
import com.tambapps.marcel.android.marshell.service.PermissionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import marcel.lang.AndroidSystem
import javax.inject.Named

@Module
@InstallIn(
  ActivityComponent::class,
  // for workers
  SingletonComponent::class)
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

  @Named("shellNotificationChannel")
  @Provides
  fun shellNotificationChannel(notificationManager: NotificationManager) = createOrGetChannel(
    notificationManager,
    id = "MarshellSession",
    name = "Marshell Notifications",
    description = "Shell session notifications"
  )

  @Named("shellAndroidNotifier")
  @Provides
  fun shellAndroidNotifier(@ApplicationContext context: Context,
                             notificationManager: NotificationManager,
                             @Named("shellNotificationChannel") shellNotificationChannel: NotificationChannel): AndroidNotifier {
    return AndroidNotifier(context, notificationManager, shellNotificationChannel)
  }

  @Named("workoutAndroidNotifier")
  @Provides
  fun workoutAndroidNotifier(@ApplicationContext context: Context,
                             notificationManager: NotificationManager,
                             @Named("workoutNotificationChannel") workoutNotificationChannel: NotificationChannel): AndroidNotifier {
    return AndroidNotifier(context, notificationManager, workoutNotificationChannel)
  }

  @Provides
  fun smsManager(@ApplicationContext context: Context): SmsManager = context.getSystemService(SmsManager::class.java)

  @Provides
  fun smsSender(smsManager: SmsManager): AndroidSmsSender = AndroidSmsSender(smsManager)

  @Named("shellAndroidSystem")
  @Provides
  fun shellAndroidSystem(
    @Named("shellAndroidNotifier") shellAndroidNotifier: AndroidNotifier,
    smsSender: AndroidSmsSender,
    permissionManager: PermissionManager
  ): AndroidSystem {
    return AndroidSystemImpl(shellAndroidNotifier, smsSender, permissionManager)
  }

  @Named("workoutAndroidSystem")
  @Provides
  fun workoutAndroidSystem(
    @Named("workoutAndroidNotifier") workoutAndroidNotifier: AndroidNotifier,
    smsSender: AndroidSmsSender,
    permissionManager: PermissionManager
  ): AndroidSystem {
    return AndroidSystemImpl(workoutAndroidNotifier, smsSender, permissionManager)
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