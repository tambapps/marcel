package com.tambapps.marcel.android.marshell.os

import android.Manifest
import com.tambapps.marcel.android.marshell.service.PermissionManager
import marcel.lang.AndroidSystem

class AndroidSystemImpl constructor(
  private val notifier: AndroidNotifier,
  private val smsSender: AndroidSmsSender,
  private val permissionManager: PermissionManager
): AndroidSystem {

  override fun notify(id: Int, title: String?, message: String?) {
    if (!notifier.areNotificationEnabled) {
      throw RuntimeException("Need permission to push notifications")
    }
    notifier.notify(id, title ?: "", message ?: "", onGoing = false)
  }

  override fun sendSms(destinationAddress: String, text: String) {
    if (!permissionManager.hasPermission(Manifest.permission.SEND_SMS)) {
      throw RuntimeException("Need permission to send SMS")
    }
    smsSender.sendSms(destinationAddress, text)
  }
}