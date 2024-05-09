package com.tambapps.marcel.android.marshell.ui.screen.work.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.work.WorkInfo
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.ui.component.EXPANDABLE_CARD_ANIMATION_SPEC
import com.tambapps.marcel.android.marshell.ui.component.ExpandableCard
import com.tambapps.marcel.android.marshell.ui.screen.work.WorkScriptCard
import com.tambapps.marcel.android.marshell.ui.screen.work.WorkStateText
import com.tambapps.marcel.android.marshell.ui.screen.work.nextRunText
import com.tambapps.marcel.android.marshell.ui.screen.work.runtimeText
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import com.tambapps.marcel.android.marshell.util.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.temporal.ChronoUnit

private val Orange = Color(0xFF925F00)

@Composable
fun WorkViewScreen(
  viewModel: WorkViewModel,
  navController: NavController,
) {
  val work = viewModel.work
  Box(
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 8.dp)
    ) {
      Header()
      if (work == null) {
        LoadingComponent()
      } else {
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
    val context = LocalContext.current

    if (work != null) {
      val isCancelable = work.state == WorkInfo.State.ENQUEUED || work.state == WorkInfo.State.RUNNING
          || work.state == WorkInfo.State.BLOCKED
      val showDialog = remember { mutableStateOf(false) }
      Fab(
        visible = true,
        modifier = Modifier.align(Alignment.BottomStart),
        onClick = { showDialog.value = true },
        color = if (isCancelable) Orange else Color.Red,
        icon = {
          Icon(
            Icons.Filled.Add,
            modifier = Modifier
              .size(23.dp)
              .rotate(45f),
            contentDescription = "Cancel",
            tint = Color.White
          )
        }
      )
      CancelOrDeleteDialog(
        viewModel = viewModel,
        work = work,
        isCancelable = isCancelable,
        navController = navController,
        show = showDialog
      )
    }

    Fab(
      visible = viewModel.scriptEdited,
      modifier = Modifier.align(Alignment.BottomEnd),
      onClick = { viewModel.validateAndSave(context) },
      icon = {
        Icon(
          painterResource(id = R.drawable.save),
          modifier = Modifier.size(23.dp),
          contentDescription = "Save",
          tint = Color.White
        )
      }
    )
  }
}

@Composable
fun CancelOrDeleteDialog(
  viewModel: WorkViewModel,
  work: ShellWork,
  isCancelable: Boolean,
  navController: NavController,
  show: MutableState<Boolean>,
  ) {
  if (!show.value) {
    return
  }
  val context = LocalContext.current
  AlertDialog(
    onDismissRequest = { show.value = false },
    dismissButton = {
      TextButton(onClick = { show.value = false }) {
        Text(text = "Cancel")
      }
    },
    confirmButton = {
      TextButton(onClick = {
        if (isCancelable) viewModel.cancelWork(context, work.name)
        else viewModel.deleteWork(context, work.name, navController)
      }) {
        Text(text = "Confirm")
      }
    },
    title = {
      Text(text = if (isCancelable) "Cancel workout?" else "Delete work?")
    },
    text = {
      Text(text = if (isCancelable) "The workout won't run anymore" else "This action is not reversible")
    }
  )
}

@Composable
fun Fab(
  visible: Boolean,
  modifier: Modifier,
  onClick: () -> Unit,
  color: Color = FloatingActionButtonDefaults.containerColor,
  icon: @Composable () -> Unit
  ) {
  AnimatedVisibility(
    modifier = modifier,
    visible = visible,
    enter = scaleIn(),
    exit = scaleOut(),
  ) {
    FloatingActionButton(
      containerColor = color,
      modifier = Modifier.padding(all = 16.dp),
      onClick = onClick, content = icon
    )
  }
}
@Composable
private fun WorkComponent(viewModel: WorkViewModel, work: ShellWork) {
  Column(
    modifier = Modifier
      .padding(horizontal = 8.dp)
      .animateContentSize(animationSpec = EXPANDABLE_CARD_ANIMATION_SPEC)
  ) {
    if (viewModel.scriptCardExpanded.value) {
      return@Column
    }
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
          .fillMaxWidth(),
        text = work.description,
        style = shellTextStyle,
        fontSize = 16.sp,
        overflow = TextOverflow.Ellipsis
      )
    }
    Text(text = runtimeText(work),
      modifier = Modifier.padding(bottom = 16.dp),
      style = shellTextStyle, fontSize = 16.sp)
    // TODO launch manually periodic work now if the duration is negative (do this on main activity, at startup)
    viewModel.durationBetweenNowAndNext?.let {
      Text(
        text = nextRunText(it),
        modifier = Modifier.padding(bottom = 16.dp),
        style = shellTextStyle, fontSize = 16.sp)
    }

    if (work.failedReason != null) {
      Text(
        modifier = Modifier
          .padding(bottom = 16.dp)
          .fillMaxWidth(),
        text = "Failure reason: ${work.failedReason}",
        style = shellTextStyle,
        fontSize = 16.sp,
        overflow = TextOverflow.Ellipsis
      )
    }

    Box(modifier = Modifier.padding(16.dp))
    if (!work.logs.isNullOrEmpty()) {
      val logsExpanded = remember { mutableStateOf(false) }
      ExpandableCard(expanded = logsExpanded, title = "Logs") {
        SelectionContainer {
          Text(text = work.logs, style = shellTextStyle)
        }
      }
    }
    Box(modifier = Modifier.padding(16.dp))
  }
  WorkScriptCard(viewModel = viewModel, readOnly = !work.isPeriodic || work.isFinished)
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