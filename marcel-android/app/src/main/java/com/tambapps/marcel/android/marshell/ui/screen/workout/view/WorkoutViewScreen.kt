package com.tambapps.marcel.android.marshell.ui.screen.workout.view

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.work.WorkInfo
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkout
import com.tambapps.marcel.android.marshell.ui.component.EXPANDABLE_CARD_ANIMATION_SPEC
import com.tambapps.marcel.android.marshell.ui.component.ExpandableCard
import com.tambapps.marcel.android.marshell.ui.screen.workout.WorkScriptCard
import com.tambapps.marcel.android.marshell.ui.screen.workout.WorkStateText
import com.tambapps.marcel.android.marshell.ui.screen.workout.nextRunText
import com.tambapps.marcel.android.marshell.ui.screen.workout.runtimeText
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import marcel.lang.Markdown
import java.io.File

private val Orange = Color(0xFFFF9800)

@Composable
fun WorkViewScreen(
  navController: NavController,
  viewModel: WorkoutViewModel = hiltViewModel()
  ) {
  val work = viewModel.workout
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
  ) {
    var columnModifier = Modifier.fillMaxSize()
      .padding(horizontal = 8.dp)

    if (!viewModel.scriptCardExpanded.value) {
      // important, otherwise the expanded script doesn't expand because it can't compute the fill height
      columnModifier = columnModifier.verticalScroll(rememberScrollState())
    }

    Column(modifier = columnModifier) {
      Header()
      if (work == null) {
        LoadingComponent()
      } else {
        WorkComponent(viewModel, work)
        Spacer(Modifier.height(128.dp)) // just so we can scroll past the buttons displayed at the bottom
        LaunchedEffect(Unit) {
          if (work.isPeriodic) {
            withContext(Dispatchers.IO) {
              while (true) {
                delay(1_000L)
                viewModel.refresh(viewModel.workout!!.name)
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
        color = if (isCancelable) Orange else Color.Red
      ) {
        Icon(
          Icons.Filled.Add,
          modifier = Modifier
            .size(23.dp)
            .rotate(45f),
          contentDescription = "Cancel",
        )
      }
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
      onClick = { viewModel.validateAndSave(context) }
    ) {
      Icon(
        painterResource(id = R.drawable.save),
        modifier = Modifier.size(23.dp),
        contentDescription = "Save",
      )
    }
  }
}

@Composable
fun CancelOrDeleteDialog(
  viewModel: WorkoutViewModel,
  work: ShellWorkout,
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
        Text(text = "Confirm", color = Color.Red)
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
private fun WorkComponent(viewModel: WorkoutViewModel, workout: ShellWorkout) {
  Column(
    modifier = Modifier.animateContentSize(animationSpec = EXPANDABLE_CARD_ANIMATION_SPEC)
  ) {
    if (viewModel.scriptCardExpanded.value) {
      return@Column
    }
    val context = LocalContext.current
    Box(modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 16.dp)) {
      Text(
        modifier = Modifier
          .fillMaxWidth(0.75f),
        text = workout.name,
        style = MaterialTheme.typography.shellTextStyle,
        fontSize = 22.sp,
        overflow = TextOverflow.Ellipsis
      )
      WorkStateText(
        shellWorkout = workout,
        fontSize = 16.sp,
        modifier = Modifier.align(Alignment.TopEnd),
      )
    }

    if (workout.description != null) {
      Text(
        modifier = Modifier
          .padding(bottom = 16.dp)
          .fillMaxWidth(),
        text = workout.description,
        style = MaterialTheme.typography.shellTextStyle,
        fontSize = 16.sp,
        overflow = TextOverflow.Ellipsis
      )
    }
    Text(text = runtimeText(workout),
      modifier = Modifier.padding(bottom = 16.dp),
      style = MaterialTheme.typography.shellTextStyle, fontSize = 16.sp)
    viewModel.durationBetweenNowAndNext?.let {
      Text(
        text = nextRunText(it),
        modifier = Modifier.padding(bottom = 16.dp),
        style = MaterialTheme.typography.shellTextStyle, fontSize = 16.sp)
    }

    if (workout.failedReason != null) {
      Text(
        modifier = Modifier
          .padding(bottom = 16.dp)
          .fillMaxWidth(),
        text = "Failure reason: ${workout.failedReason}",
        style = MaterialTheme.typography.shellTextStyle,
        fontSize = 16.sp,
        overflow = TextOverflow.Ellipsis
      )
    }
    Box(modifier = Modifier.padding(16.dp))

    if (!workout.initScripts.isNullOrEmpty()) {
      val initScripts = remember { workout.initScripts.map { File(it) } }
      Text(text = "Warmup scripts", style = MaterialTheme.typography.titleMedium)
      for (initScript in initScripts) {
        Text(text = "- " + initScript.name,
          modifier = Modifier.clickable { Toast.makeText(context, initScript.path, Toast.LENGTH_SHORT).show() },
          style = MaterialTheme.typography.bodyMedium)
      }
      Box(modifier = Modifier.padding(16.dp))
    }
    if (!workout.logs.isNullOrEmpty()) {
      val logsExpanded = remember { mutableStateOf(false) }
      ExpandableCard(expanded = logsExpanded, title = "Logs") {
        SelectionContainer {
          Text(text = workout.logs, style = MaterialTheme.typography.shellTextStyle)
        }
      }
      Box(modifier = Modifier.padding(16.dp))
    }
  }
  // should stay outside of the Column in order to expand over everything else
  WorkScriptCard(viewModel = viewModel, readOnly = true, title = "Workout script")

  if (workout.result != null) {
    Box(modifier = Modifier.padding(16.dp))
    val result = workout.result
    val isMarkdown = workout.resultClassName == Markdown::class.java.name
    SelectionContainer {
      if (isMarkdown) {
        viewModel.mdComposer.Markdown(node = Markdown.PARSER.parse(result))
      } else {
        Text(text = result, style = MaterialTheme.typography.shellTextStyle, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
      }
    }
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
    style = MaterialTheme.typography.shellTextStyle,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 16.dp)
      .height(TopBarHeight), textAlign = TextAlign.Center,
    fontSize = 22.sp
  )
}