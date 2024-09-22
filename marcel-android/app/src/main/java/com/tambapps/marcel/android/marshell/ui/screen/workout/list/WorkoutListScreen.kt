package com.tambapps.marcel.android.marshell.ui.screen.workout.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tambapps.marcel.android.marshell.Routes
import com.tambapps.marcel.android.marshell.room.entity.ShellWorkout
import com.tambapps.marcel.android.marshell.ui.screen.workout.WorkStateText
import com.tambapps.marcel.android.marshell.ui.screen.workout.nextRunText
import com.tambapps.marcel.android.marshell.ui.screen.workout.runtimeText
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun WorksListScreen(
  navController: NavController,
  viewModel: WorkoutListViewModel = hiltViewModel()
  ) {
  LaunchedEffect(Unit) {
    withContext(Dispatchers.IO) {
      while (true) {
        delay(1_000L)
        viewModel.refresh()
      }
    }
  }
  Box(modifier = Modifier
    .fillMaxSize().padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
    contentAlignment = Alignment.BottomEnd) {
    Column(modifier = Modifier.fillMaxSize()) {
      Header()
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        items(viewModel.works) { shellWork ->
          ShellWorkItem(shellWork, navController)
        }
      }
    }

    FloatingActionButton(
      modifier = Modifier.padding(all = 16.dp),
      onClick = {
        navController.navigate(Routes.WORKOUT_CREATE)
      }
    ) {
      Icon(
        Icons.Filled.Add,
        modifier = Modifier.size(23.dp),
        contentDescription = "Save",
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShellWorkItem(shellWorkout: ShellWorkout, navController: NavController) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .combinedClickable(
        onClick = {
          navController.navigate(Routes.WORKOUT_VIEW + "/" + shellWorkout.name)
        },
        onLongClick = {}
      )
  ) {
    Box(modifier = Modifier.padding(8.dp))
    Text(text = shellWorkout.name, style = MaterialTheme.typography.shellTextStyle, fontSize = 18.sp)
    Text(text = runtimeText(shellWorkout), style = MaterialTheme.typography.shellTextStyle, fontSize = 16.sp)

    if (!shellWorkout.isFinished) {
      shellWorkout.durationBetweenNowAndNext?.let {
        Text(text = nextRunText(it), style = MaterialTheme.typography.shellTextStyle, fontSize = 16.sp)
      }
    }

    WorkStateText(workout = shellWorkout, modifier = Modifier.align(Alignment.End), fontSize = 14.sp)
    Box(modifier = Modifier.padding(8.dp))
    HorizontalDivider()
  }
}

@Composable
private fun Header() {
  Text(
    text = "Shell Workouts",
    style = MaterialTheme.typography.shellTextStyle,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 16.dp)
      .height(TopBarHeight), textAlign = TextAlign.Center,
    fontSize = 22.sp
    )
}