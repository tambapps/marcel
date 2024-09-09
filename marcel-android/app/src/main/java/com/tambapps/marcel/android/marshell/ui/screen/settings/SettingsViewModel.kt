package com.tambapps.marcel.android.marshell.ui.screen.settings

import android.Manifest
import android.app.NotificationManager
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tambapps.marcel.android.marshell.service.PermissionManager
import com.tambapps.marcel.android.marshell.service.PreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SettingsViewModel @Inject constructor(
  val permissionManager: PermissionManager,
  private val notificationManager: NotificationManager,
  private val preferencesDataStore: PreferencesDataStore,
  @Named("initScriptFile")
  val initScriptFile: File
  ): ViewModel() {

  private val scope = CoroutineScope(Dispatchers.IO)
  var canManageFiles by mutableStateOf(false)
    private set

  var canSendSms by mutableStateOf(false)
    private set
  var areNotificationEnabled by mutableStateOf(false)
    private set

  var homeDirectory by mutableStateOf<File?>(null)
    private set

  fun updateHomeDirectory(file: File) {
    scope.launch {
      preferencesDataStore.setHomeDirectory(file)
      refresh()
    }
  }

  fun refresh() {
    canManageFiles = Environment.isExternalStorageManager()
    areNotificationEnabled = notificationManager.areNotificationsEnabled()
    canSendSms = permissionManager.hasPermission(Manifest.permission.SEND_SMS)
    scope.launch {
      homeDirectory = preferencesDataStore.homeDirectory.first()
    }
  }
}