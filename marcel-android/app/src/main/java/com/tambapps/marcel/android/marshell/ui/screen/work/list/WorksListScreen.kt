package com.tambapps.marcel.android.marshell.ui.screen.work.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.WorkInfo
import com.tambapps.marcel.android.marshell.Routes
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import com.tambapps.marcel.android.marshell.util.TimeUtils
import com.tambapps.marcel.android.marshell.work.ShellWorkManager
import java.time.Duration
import java.time.temporal.ChronoUnit


@Composable
fun WorksListScreen(
  viewModel: WorksListViewModel,
  navController: NavController
) {
  Box(modifier = Modifier
    .fillMaxSize(),
    contentAlignment = Alignment.BottomEnd) {
    Column(modifier = Modifier.fillMaxSize()) {
      Header()
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        viewModel.works.forEach { shellWork ->
          item {
            ShellWorkItem(shellWork)
          }
        }
      }
    }

    FloatingActionButton(
      modifier = Modifier.padding(all = 16.dp),
      onClick = {
        navController.navigate(Routes.WORK_CREATE)
      }
    ) {
      Icon(
        Icons.Filled.Add,
        modifier = Modifier.size(23.dp),
        contentDescription = "Save",
        tint = Color.White
      )
    }
  }
}

@Composable
fun ShellWorkItem(shellWork: ShellWork) {
  Column(
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(text = shellWork.name, style = shellTextStyle, fontSize = 18.sp)
    Text(text = runtimeText(shellWork), style = shellTextStyle, fontSize = 16.sp)

    shellWork.durationBetweenNowAndNext?.let {
      Text(text = "next run in " + TimeUtils.humanReadableFormat(it, ChronoUnit.SECONDS), style = shellTextStyle, fontSize = 16.sp)
    }

    Text(text = stateText(shellWork), color = stateColor(shellWork), modifier = Modifier.align(Alignment.End), textAlign = TextAlign.Center, fontSize = 14.sp)
  }
}

val Orange = Color(0xFFFFA500)
val SkyBlue = Color(0xFF87CEEB)


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

private fun runtimeText(work: ShellWork) = when {
  work.startTime != null ->
    if (work.isFinished) "ran ${TimeUtils.smartToString(work.startTime)} for ${TimeUtils.humanReadableFormat(Duration.between(work.startTime, work.endTime))}"
    else if (work.isPeriodic) when {
      work.state == WorkInfo.State.RUNNING -> "started ${TimeUtils.smartToString(work.startTime)}"
      work.endTime != null -> "last ran ${TimeUtils.smartToString(work.startTime)} for ${TimeUtils.humanReadableFormat(Duration.between(work.startTime, work.endTime))}"
      else -> "has not ran yet"
    }
    else "started " + TimeUtils.smartToString(work.startTime)
  work.scheduledAt != null -> "Scheduled for " + TimeUtils.smartToString(work.scheduledAt)
  work.failedReason == null -> "has not ran yet"
  else -> "has not ran yet"
}

@Composable
private fun Header() {
  Text(
    text = "Shell Works",
    style = shellTextStyle,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 16.dp)
      .height(TopBarHeight), textAlign = TextAlign.Center,
    fontSize = 22.sp
    )
}