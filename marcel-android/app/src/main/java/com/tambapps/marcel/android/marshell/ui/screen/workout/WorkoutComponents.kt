package com.tambapps.marcel.android.marshell.ui.screen.workout

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import com.tambapps.marcel.android.marshell.FilePickerActivity
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkout
import com.tambapps.marcel.android.marshell.ui.component.ExpandableCard
import com.tambapps.marcel.android.marshell.ui.component.ScriptTextField
import com.tambapps.marcel.android.marshell.ui.screen.ScriptCardEditorViewModel
import com.tambapps.marcel.android.marshell.util.TimeUtils
import java.time.Duration
import java.time.temporal.ChronoUnit

@Composable
fun WorkScriptCard(viewModel: ScriptCardEditorViewModel, readOnly: Boolean = false, title: String = "Script") {
  val tintColor = if (viewModel.scriptTextError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
  ExpandableCard(expanded = viewModel.scriptCardExpanded,
    title = title,
    titleColor = tintColor,
    logoTint = tintColor,
    additionalLogos = if (readOnly) null else {
      {
        val context = LocalContext.current
        val pickFileLauncher = FilePickerActivity.rememberFilePickerForActivityResult { file ->
          viewModel.loadScript(context, file)
        }
        IconButton(
          modifier = Modifier
            .weight(1f)
            .size(24.dp),
          onClick = { pickFileLauncher.launch(FilePickerActivity.Args()) }) {
          Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.folder),
            contentDescription = "Pick file",
            tint = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }) {
    val focusRequester = remember { FocusRequester() }
    // request focus when card is opened
    LaunchedEffect(viewModel.scriptCardExpanded.value) {
      if (viewModel.scriptCardExpanded.value) {
        focusRequester.requestFocus()
      }
    }
    ScriptTextField(viewModel = viewModel, readOnly = readOnly, modifier = Modifier
      .weight(1f)
      .padding(top = 8.dp), focusRequester = focusRequester)
  }
}

@Composable
fun WorkStateText(shellWorkout: ShellWorkout, modifier: Modifier = Modifier, fontSize: TextUnit = TextUnit.Unspecified) {
  Text(text = stateText(shellWorkout), color = stateColor(shellWorkout), modifier = modifier, textAlign = TextAlign.Center, fontSize = fontSize)
}

fun nextRunText(durationBetweenNowAndNext: Duration): String {
  return if (durationBetweenNowAndNext.isNegative) "Will run soon" else "next run in " + TimeUtils.humanReadableFormat(durationBetweenNowAndNext, ChronoUnit.SECONDS)
}

fun runtimeText(workout: ShellWorkout) = when {
  workout.startTime != null ->
    if (workout.isFinished) "ran ${TimeUtils.smartToString(workout.startTime)} for ${
      TimeUtils.humanReadableFormat(
        Duration.between(workout.startTime, workout.endTime))}"
    else if (workout.isPeriodic) when {
      workout.state == WorkInfo.State.RUNNING -> "started ${TimeUtils.smartToString(workout.startTime)}"
      workout.endTime != null -> "last ran ${TimeUtils.smartToString(workout.startTime)} for ${
        TimeUtils.humanReadableFormat(
          Duration.between(workout.startTime, workout.endTime))}"
      else -> "has not ran yet"
    }
    else "started " + TimeUtils.smartToString(workout.startTime)
  workout.scheduledAt != null -> "Scheduled for " + TimeUtils.smartToString(workout.scheduledAt)
  workout.failedReason == null -> "has not ran yet"
  else -> "has not ran yet"
}

private val Orange = Color(0xFFFFA500)
private val SkyBlue = Color(0xFF87CEEB)
// private val DeepBlue = Color(0xFF00338B) it was for light theme when I supported it

private fun stateText(work: ShellWorkout): String {
  return if (work.period != null && !work.state.isFinished) {
    val unitStr = work.period.unit.name.lowercase()
    if (work.state == WorkInfo.State.RUNNING) "RUNNING"
    else if (work.period.amount == 1) "PERIODIC\n(every ${unitStr.removeSuffix("s")})"
    else "PERIODIC\n(every ${work.period.amount} $unitStr)"
  } else work.state.name
}

@Composable
private fun stateColor(work: ShellWorkout) = when {
  work.state == WorkInfo.State.SUCCEEDED -> Color.Green
  work.state == WorkInfo.State.CANCELLED -> Orange
  work.state == WorkInfo.State.FAILED -> Color.Red
  work.isPeriodic || work.state == WorkInfo.State.RUNNING -> SkyBlue
  else -> MaterialTheme.colorScheme.onSurface
}