package com.tambapps.marcel.android.marshell.ui.screen.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tambapps.marcel.android.marshell.BuildConfig
import com.tambapps.marcel.android.marshell.Routes
import com.tambapps.marcel.android.marshell.ui.component.EnabledNotificationsDialog
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight
import com.tambapps.marcel.android.marshell.util.LifecycleStateListenerEffect
import java.net.URLEncoder


val itemStyle = TextStyle.Default.copy(
  color = Color.White,
  fontSize = 16.sp,
)
val itemDescriptionStyle = TextStyle.Default.copy(
  color = Color.White,
  fontSize = 14.sp,
)
val paddingStart = 40.dp

@Composable
fun SettingsScreen(
  navController: NavController,
  viewModel: SettingsViewModel = hiltViewModel()
  ) {
  val context = LocalContext.current
  val permissionManager = viewModel.permissionManager

  LifecycleStateListenerEffect(
    onResume = viewModel::refresh
  )

  val scrollState = rememberScrollState()
  Column(modifier = Modifier
    .fillMaxSize()
    .padding(top = TopBarHeight)
    .scrollable(scrollState, Orientation.Vertical)
    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
  ) {

    SectionTitle(text = "Shell")
    SettingItem(
      text = "Initialization script",
      description = "Configure a script that will be executed at every shell session startup",
      onClick = { navController.navigate(Routes.EDITOR + "?" + Routes.FILE_ARG + "=" + URLEncoder.encode(viewModel.initScriptFile.canonicalPath, "UTF-8")) })


    SectionTitle(text = "Permissions")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // asking permission is only required since Android TIRAMISU
      val showEnableNotificationDialog = remember { mutableStateOf(false) }
      SettingItem(
        text = "Notifications",
        description = "Allow your scripts/workouts to push notifications",
        onClick = {
          if (viewModel.areNotificationEnabled) {
            permissionManager.startNotificationSettingsActivity(context)
          } else {
            showEnableNotificationDialog.value = true
          }
        },
        checked = viewModel.areNotificationEnabled
      )
      val requestNotificationsPermission = permissionManager.rememberPermissionRequestActivityLauncher()
      EnabledNotificationsDialog(permissionManager, showEnableNotificationDialog, requestNotificationsPermission,
        description = null)
    }
    SettingItem(
      text = "Manage files",
      description = "Manage files of your device within Marcel scripts/workouts",
      onClick = { askManageFilePermission(context) },
      checked = viewModel.canManageFiles
    )

    val smsPermissionLauncher = permissionManager.rememberPermissionRequestActivityLauncher()
    SettingItem(
      text = "Send SMS",
      description = "Allow your scripts/workouts to send SMS",
      onClick = {
        if (viewModel.canSendSms) permissionManager.openPermissionSettings(context)
        else smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
      },
      checked = viewModel.canSendSms
    )

    /*
    SectionTitle(text = "Dependency Management")
    SettingItem(
      text = "Maven Repository",
      description = "Manage fetched Maven artifacts",
      onClick = {  }
    )
     */
  }
}

fun askManageFilePermission(context: Context) {
  context.startActivity(Intent(
    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
    Uri.parse("package:${BuildConfig.APPLICATION_ID}")
  ))
}

@Composable
private fun SectionTitle(text: String) {
  Text(
    modifier = Modifier.padding(start = paddingStart, top = 16.dp, bottom = 8.dp),
    text = text,
    style = MaterialTheme.typography.titleMedium,
    color = MaterialTheme.colorScheme.primary,
    fontWeight = FontWeight.SemiBold
  )
}

@Composable
private fun SettingItem(
  text: String,
  description: String? = null,
  onClick: () -> Unit,
  checked: Boolean? = null,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .defaultMinSize(minHeight = 50.dp)
      .clickable(onClick = onClick),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = Modifier.padding(start = paddingStart))
    Column(modifier = Modifier
      .weight(1f)
      .padding(vertical = 16.dp)) {
      Text(text = text, style = itemStyle)
      if (description != null) {
        Text(text = description, style = itemDescriptionStyle, modifier = Modifier.padding(top = 4.dp))
      }
    }
    if (checked != null) {
      Switch(checked = checked, onCheckedChange = { onClick.invoke() })
    }
  }
}