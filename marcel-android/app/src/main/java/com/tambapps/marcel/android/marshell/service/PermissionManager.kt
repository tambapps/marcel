package com.tambapps.marcel.android.marshell.service

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class PermissionManager @Inject constructor(
  private val preferencesDataStore: PreferencesDataStore
) {

  private val ioScope = CoroutineScope(Dispatchers.IO)

  val canAskNotificationsPermission
    @Composable
    get() = preferencesDataStore.askedNotificationPermissionsState.value


  fun setAskedPermissionsPreferences() = ioScope.launch {
    preferencesDataStore.setAskedPermissionsPreferences(true)
  }

  fun startNotificationSettingsActivity(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    context.startActivity(intent, null)
  }

  @Composable
  fun rememberPermissionRequestActivityLauncher(): ManagedActivityResultLauncher<String, Boolean> {
    val context = LocalContext.current
    return rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { granted ->
      Toast.makeText(context, "Permission " + (if (granted) "granted" else "not granted"), Toast.LENGTH_SHORT).show()
    }

  }
}