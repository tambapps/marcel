package com.tambapps.marcel.android.marshell.os

import android.Manifest
import com.tambapps.marcel.android.marshell.BuildConfig
import com.tambapps.marcel.android.marshell.service.PermissionManager
import marcel.lang.android.AndroidMessage
import marcel.lang.android.AndroidSystemHandler
import marcel.lang.android.PermissionDeniedException

class AndroidSystemImpl constructor(
  private val notifier: AndroidNotifier,
  private val smsSender: AndroidSmsSender,
  private val permissionManager: PermissionManager
): AndroidSystemHandler {

  override fun notify(id: Int, title: String?, message: String?) {
    if (!notifier.areNotificationEnabled) {
      throw PermissionDeniedException("Permission to push notifications was not granted")
    }
    notifier.notify(id, title ?: "", message ?: "", onGoing = false)
  }

  override fun sendSms(destinationAddress: String, text: String): AndroidMessage {
    if (!permissionManager.hasPermission(Manifest.permission.SEND_SMS)) {
      throw PermissionDeniedException("Permission to send SMS was not granted")
    }
    if (!BuildConfig.SMS_ENABLED) {
      throw PermissionDeniedException("SMS sending is not enabled on this build of this app")
    }
    return smsSender.sendSms(destinationAddress, text)
  }

  override fun listSms(page: Int, pageSize: Int) = smsSender.list(page, pageSize)

  // TODO DELETE ME
  fun withNotifier(notifier: AndroidNotifier) = AndroidSystemImpl(notifier, smsSender, permissionManager)
}