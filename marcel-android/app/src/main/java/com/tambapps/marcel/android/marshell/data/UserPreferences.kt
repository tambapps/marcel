package com.tambapps.marcel.android.marshell.data

data class UserPreferences(val shellPreferences: ShellPreferences)

data class ShellPreferences(val singleLineInput: Boolean) {
  companion object {
    val DEFAULT = ShellPreferences(singleLineInput = false)
  }
}