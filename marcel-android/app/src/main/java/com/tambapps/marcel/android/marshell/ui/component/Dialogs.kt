package com.tambapps.marcel.android.marshell.ui.component

import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import com.tambapps.marcel.android.marshell.service.PermissionManager

@Composable
fun EnabledNotificationsDialog(
  permissionManager: PermissionManager,
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
      val canAskNotificationsPermission = permissionManager.canAskNotificationsPermission
      val context = LocalContext.current
      TextButton(onClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && canAskNotificationsPermission) {
          permissionManager.setAskedPermissionsPreferences()
          requestNotificationsPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
          permissionManager.startNotificationSettingsActivity(context)
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
