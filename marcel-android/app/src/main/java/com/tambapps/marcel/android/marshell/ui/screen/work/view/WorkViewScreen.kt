package com.tambapps.marcel.android.marshell.ui.screen.work.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.ui.screen.work.WorkStateText
import com.tambapps.marcel.android.marshell.ui.screen.work.runtimeText
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import com.tambapps.marcel.android.marshell.util.TimeUtils
import java.time.temporal.ChronoUnit


@Composable
fun WorkViewScreen(
  viewModel: WorkViewModel
) {
  Box(
    modifier = Modifier
      .fillMaxSize(),
    contentAlignment = Alignment.BottomEnd
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
        WorkComponent(viewModel, viewModel.work!!)
      }
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
      WorkStateText(shellWork = work, fontSize = 16.sp, modifier = Modifier.align(Alignment.TopEnd))
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
    Text(text = runtimeText(work), style = shellTextStyle, fontSize = 16.sp)
    work.durationBetweenNowAndNext?.let {
      Text(text = "next run in " + TimeUtils.humanReadableFormat(it, ChronoUnit.SECONDS), style = shellTextStyle, fontSize = 16.sp)
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
  // TODO write help button that opens a dialog and explain shell works to user
  Text(
    text = "View Workout",
    style = shellTextStyle,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 16.dp)
      .height(TopBarHeight), textAlign = TextAlign.Center,
    fontSize = 22.sp
  )
}