package com.tambapps.marcel.android.marshell.ui.screen.work

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.work.WorkInfo
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.util.TimeUtils
import java.time.Duration


@Composable
fun WorkStateText(shellWork: ShellWork, modifier: Modifier = Modifier,
                  textAlign: TextAlign = TextAlign.Unspecified, fontSize: TextUnit = TextUnit.Unspecified) {
  Text(text = stateText(shellWork), color = stateColor(shellWork), modifier = modifier, textAlign = textAlign, fontSize = fontSize)
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
    if (work.state == WorkInfo.State.RUNNING) "RUNNING"
    else if (work.period.amount == 1) "PERIODIC\n(every ${work.period.unit.toString().removeSuffix("s")})"
    else "PERIODIC\n(every ${work.period.amount} ${work.period.unit})"
  } else work.state.name
}

private fun stateColor(work: ShellWork) = when {
  work.state == WorkInfo.State.SUCCEEDED -> Color.Green
  work.state == WorkInfo.State.CANCELLED -> Orange
  work.state == WorkInfo.State.FAILED -> Color.Red
  work.isPeriodic || work.state == WorkInfo.State.RUNNING -> SkyBlue
  else -> Color.White
}