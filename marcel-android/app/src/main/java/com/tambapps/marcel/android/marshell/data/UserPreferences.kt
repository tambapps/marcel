package com.tambapps.marcel.android.marshell.data

data class UserPreferences(
  val askedNotificationPermissions: Boolean,
  val homeDirectory: String?
) {
  companion object {
    val DEFAULT = UserPreferences(
      askedNotificationPermissions = false,
      homeDirectory = null
    )

  }
}