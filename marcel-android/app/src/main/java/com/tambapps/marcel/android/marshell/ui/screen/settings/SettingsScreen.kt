package com.tambapps.marcel.android.marshell.ui.screen.settings

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import com.tambapps.marcel.android.marshell.BuildConfig
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight


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
fun SettingsScreen(viewModel: SettingsViewModel) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
  DisposableEffect(lifecycleState) {
    when (lifecycleState) {
      Lifecycle.State.DESTROYED -> {}
      Lifecycle.State.INITIALIZED -> {}
      Lifecycle.State.CREATED -> {}
      Lifecycle.State.STARTED -> {}
      Lifecycle.State.RESUMED -> viewModel.refresh()
    }
    onDispose {  }
  }

  val colorScheme = MaterialTheme.colorScheme
  val sectionStyle = remember {
    TextStyle.Default.copy(
      color = colorScheme.primary,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold
    )
  }

  val scrollState = rememberScrollState()
  Column(modifier = Modifier
    .fillMaxSize()
    .padding(top = TopBarHeight)
    .scrollable(scrollState, Orientation.Vertical)
  ) {

    SectionTitle(text = "Shell", sectionStyle = sectionStyle)
    SettingItem(
      text = "Initialization script",
      description = "Configure a script that will be executed at every shell session startup",
      onClick = { /* TODO */ })


    SectionTitle(text = "Permissions", sectionStyle = sectionStyle)
    SettingItem(
      text = "Manage files",
      description = "Manage files of your device within Marcel scripts",
      onClick = {
        context.startActivity(Intent(
          Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
          Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        ))
      },
      checked = viewModel.canManageFiles
    )

    SectionTitle(text = "Dependency Management", sectionStyle = sectionStyle)
    SettingItem(
      text = "Maven Repository",
      description = "Manage fetched Maven artifacts",
      onClick = { /* TODO */ }
    )
  }
}

@Composable
private fun SectionTitle(
  text: String,
  sectionStyle: TextStyle) {
  Text(
    modifier = Modifier.padding(start = paddingStart, top = 16.dp, bottom = 8.dp),
    text = text,
    style = sectionStyle
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
    Column(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
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