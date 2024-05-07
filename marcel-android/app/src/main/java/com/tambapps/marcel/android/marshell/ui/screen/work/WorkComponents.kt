package com.tambapps.marcel.android.marshell.ui.screen.work

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.ui.component.ExpandableCard
import com.tambapps.marcel.android.marshell.util.TimeUtils
import java.time.Duration

@Composable
fun WorkScriptCard(viewModel: ScriptCardViewModel, readOnly: Boolean = false) {
  ExpandableCard(expanded = viewModel.scriptCardExpanded, title = "Script",
    additionalLogos = if (readOnly) null else {
      {
        val context = LocalContext.current
        val pickPictureLauncher = rememberLauncherForActivityResult(
          ActivityResultContracts.GetContent()
        ) { imageUri ->
          viewModel.loadScript(context, imageUri)
        }
        IconButton(
          modifier = Modifier
            .weight(1f)
            .size(24.dp),
          onClick = { pickPictureLauncher.launch("*/*") }) {
          Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.folder),
            contentDescription = "Pick file",
            tint = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }) {
    // TODO find a way to add line numbers. same on editor screen
    TextField(
      // this is a hack to prevent this https://stackoverflow.com/questions/76287857/when-parent-of-textfield-is-clickable-hardware-enter-return-button-triggers-its
      modifier = Modifier
        .fillMaxWidth()
        .onKeyEvent { it.type == KeyEventType.KeyUp && it.key == Key.Enter },
      value = viewModel.scriptTextInput,
      readOnly = readOnly,
      onValueChange = viewModel::onScriptTextChange,
      visualTransformation = viewModel,
      isError = viewModel.scriptTextError != null,
      supportingText = viewModel.scriptTextError?.let { error -> {
        Text(
          modifier = Modifier.fillMaxWidth(),
          text = error,
          color = MaterialTheme.colorScheme.error
        )
      }},
    )
  }

}

@Composable
fun WorkStateText(shellWork: ShellWork, modifier: Modifier = Modifier, fontSize: TextUnit = TextUnit.Unspecified) {
  Text(text = stateText(shellWork), color = stateColor(shellWork), modifier = modifier, textAlign = TextAlign.Center, fontSize = fontSize)
}

fun runtimeText(work: ShellWork) = when {
  work.startTime != null ->
    if (work.isFinished) "ran ${TimeUtils.smartToString(work.startTime)} for ${
      TimeUtils.humanReadableFormat(
        Duration.between(work.startTime, work.endTime))}"
    else if (work.isPeriodic) when {
      work.state == WorkInfo.State.RUNNING -> "started ${TimeUtils.smartToString(work.startTime)}"
      work.endTime != null -> "last ran ${TimeUtils.smartToString(work.startTime)} for ${
        TimeUtils.humanReadableFormat(
          Duration.between(work.startTime, work.endTime))}"
      else -> "has not ran yet"
    }
    else "started " + TimeUtils.smartToString(work.startTime)
  work.scheduledAt != null -> "Scheduled for " + TimeUtils.smartToString(work.scheduledAt)
  work.failedReason == null -> "has not ran yet"
  else -> "has not ran yet"
}

private val Orange = Color(0xFFFFA500)
private val SkyBlue = Color(0xFF87CEEB)

private fun stateText(work: ShellWork): String {
  return if (work.period != null && !work.state.isFinished) {
    val unitStr = work.period.unit.name.lowercase()
    if (work.state == WorkInfo.State.RUNNING) "RUNNING"
    else if (work.period.amount == 1) "PERIODIC\n(every ${unitStr.removeSuffix("s")})"
    else "PERIODIC\n(every ${work.period.amount} $unitStr)"
  } else work.state.name
}

private fun stateColor(work: ShellWork) = when {
  work.state == WorkInfo.State.SUCCEEDED -> Color.Green
  work.state == WorkInfo.State.CANCELLED -> Orange
  work.state == WorkInfo.State.FAILED -> Color.Red
  work.isPeriodic || work.state == WorkInfo.State.RUNNING -> SkyBlue
  else -> Color.White
}