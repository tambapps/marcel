package com.tambapps.marcel.android.marshell.ui.screen.settings

import android.app.NotificationManager
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.service.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SettingsViewModel @Inject constructor(
  val permissionManager: PermissionManager,
  private val notificationManager: NotificationManager,
  @Named("initScriptFile")
  val initScriptFile: File
  ): ViewModel() {

  var canManageFiles by mutableStateOf(false)
    private set
  var areNotificationEnabled by mutableStateOf(false)
    private set

  init {
    refresh()
  }

  fun refresh() {
    canManageFiles = Environment.isExternalStorageManager()
    areNotificationEnabled = notificationManager.areNotificationsEnabled()
  }


}