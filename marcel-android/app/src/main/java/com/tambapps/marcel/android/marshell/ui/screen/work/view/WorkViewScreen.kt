package com.tambapps.marcel.android.marshell.ui.screen.work.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.ui.component.ExpandableCard
import com.tambapps.marcel.android.marshell.ui.screen.work.WorkScriptCard
import com.tambapps.marcel.android.marshell.ui.screen.work.WorkStateText
import com.tambapps.marcel.android.marshell.ui.screen.work.runtimeText
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import com.tambapps.marcel.android.marshell.util.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.temporal.ChronoUnit


@Composable
fun WorkViewScreen(
  viewModel: WorkViewModel
) {
  Box(
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 8.dp)
    ) {
      Header()
      if (viewModel.work == null) {
        LoadingComponent()
      } else {
        val work = viewModel.work!!
        WorkComponent(viewModel, work)
        LaunchedEffect(Unit) {
          if (work.isPeriodic) {
            withContext(Dispatchers.IO) {
              while (true) {
                delay(1_000L)
                viewModel.refresh(viewModel.work!!.name)
              }
            }
          }
        }

      }
    }
    SaveFab(viewModel = viewModel, modifier = Modifier.align(Alignment.BottomEnd))
  }
}

@Composable
fun SaveFab(viewModel: WorkViewModel, modifier: Modifier) {
  AnimatedVisibility(
    modifier = modifier,
    visible = viewModel.scriptEdited,
    enter = scaleIn(),
    exit = scaleOut(),
  ) {
    val context = LocalContext.current
    FloatingActionButton(
      modifier = Modifier.padding(all = 16.dp),
      onClick = {
        viewModel.validateAndSave(context)
      }
    ) {
      Icon(
        painterResource(id = R.drawable.save),
        modifier = Modifier.size(23.dp),
        contentDescription = "Save",
        tint = Color.White
      )
    }
  }
}
@Composable
private fun WorkComponent(viewModel: WorkViewModel, work: ShellWork) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 8.dp)
  ) {
    Box(modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 16.dp)) {
      Text(
        modifier = Modifier
          .fillMaxWidth(0.75f),
        text = work.name,
        style = shellTextStyle,
        fontSize = 22.sp,
        overflow = TextOverflow.Ellipsis
      )
      WorkStateText(
        shellWork = work,
        fontSize = 16.sp,
        modifier = Modifier.align(Alignment.TopEnd),
      )
    }

    if (work.description != null) {
      Text(
        modifier = Modifier
          .padding(bottom = 16.dp)
          .fillMaxWidth(0.75f),
        text = work.description,
        style = shellTextStyle,
        fontSize = 16.sp,
        overflow = TextOverflow.Ellipsis
      )
    }
    Text(text = runtimeText(work),
      modifier = Modifier.padding(bottom = 16.dp),
      style = shellTextStyle, fontSize = 16.sp)
    viewModel.durationBetweenNowAndNext?.let {
      Text(
        text = "next run in " + TimeUtils.humanReadableFormat(it, ChronoUnit.SECONDS),
        modifier = Modifier.padding(bottom = 16.dp),
        style = shellTextStyle, fontSize = 16.sp)
    }

    Box(modifier = Modifier.padding(16.dp))
    if (work.logs != null) {
      val logsExpanded = remember { mutableStateOf(false) }
      ExpandableCard(expanded = logsExpanded, title = "Logs") {
        Text(text = work.logs, style = shellTextStyle)
      }
    }
    Box(modifier = Modifier.padding(16.dp))
    WorkScriptCard(viewModel = viewModel, readOnly = !work.isPeriodic)
  }

}
@Composable
private fun LoadingComponent() {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    LinearProgressIndicator()
  }
}
@Composable
private fun Header() {
  Text(
    text = "Workout",
    style = shellTextStyle,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 16.dp)
      .height(TopBarHeight), textAlign = TextAlign.Center,
    fontSize = 22.sp
  )
}