package com.tambapps.marcel.android.marshell.ui.component

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import com.tambapps.marcel.android.marshell.service.PreferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun EnabledNotificationsDialog(
  preferencesDataStore: PreferencesDataStore,
  show: MutableState<Boolean>,
  requestNotificationsPermission: ManagedActivityResultLauncher<String, Boolean>,
  description: String?
) {
  if (!show.value) return
  AlertDialog(
    title = {
      Text(text = "Allow notifications")
    },
    text = if (description != null) {
      {Text(text = description)}
    } else null,
    onDismissRequest = { show.value = false },
    confirmButton = {
      val canAskNotificationsPermission = preferencesDataStore.askedNotificationPermissionsState.value
      val context = LocalContext.current
      TextButton(onClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && canAskNotificationsPermission) {
          CoroutineScope(Dispatchers.IO).launch {
            preferencesDataStore.setAskedPermissionsPreferences(true)
          }
          requestNotificationsPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
          startNotificationSettingsActivity(context)
        }
        show.value = false
      }) {
        Text(text = "Allow")
      }
    },
    dismissButton = {
      TextButton(onClick = { show.value = false }) {
        Text(text = "Cancel")
      }
    }
  )
}

fun startNotificationSettingsActivity(context: Context) {
  val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
  context.startActivity(intent, null)
}
