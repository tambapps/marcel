package com.tambapps.marcel.android.marshell.data

data class UserPreferences(
  val askedNotificationPermissions: Boolean
) {
  companion object {
    val DEFAULT = UserPreferences(
      askedNotificationPermissions = false
    )

  }
}