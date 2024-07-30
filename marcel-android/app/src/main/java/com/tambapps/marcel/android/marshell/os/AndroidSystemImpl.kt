package com.tambapps.marcel.android.marshell.os

import marcel.lang.AndroidSystem

class AndroidSystemImpl(
  private val notifier: AndroidNotifier
): AndroidSystem {

  override fun notify(id: Int, title: String?, message: String?) {
    if (!notifier.areNotificationEnabled) {
      throw RuntimeException("Notifications are not enabled")
    }
    notifier.notify(id, title ?: "", message ?: "", onGoing = false)
  }
}