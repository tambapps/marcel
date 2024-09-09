package com.tambapps.marcel.android.marshell.ui.screen.documentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tambapps.marcel.android.marshell.ui.component.MarkdownComposer
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight
import com.tambapps.marcel.android.marshell.ui.theme.TopBarIconSize

@Composable
fun DocumentationScreen(
  viewModel: DocumentationViewModel = hiltViewModel(),
  onGoPrevious: (() -> Unit)?,
  onGoNext: (() -> Unit)?
) {
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    viewModel.fetchPage(context)
  }
  Column(modifier = Modifier
    .fillMaxSize()) {
    TopBar()
    Column(modifier = Modifier
      .fillMaxSize()
      .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
      .verticalScroll(rememberScrollState())) {
      val node = viewModel.node
      if (node == null) {
        LinearProgressIndicator(
          modifier = Modifier.fillMaxWidth().height(2.dp)
        )
        return
      }
      viewModel.Markdown(node)

      Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
        if (onGoPrevious != null) {
          Button(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = onGoPrevious
          ) {
            Icon(
              modifier = Modifier.size(TopBarIconSize),
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = null,
            )
          }
        }

        if (onGoNext != null) {
          Button(
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = onGoNext
          ) {
            Icon(
              modifier = Modifier.size(TopBarIconSize).rotate(180f),
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = null,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TopBar() {
  Box(modifier = Modifier
    .fillMaxWidth()
    .height(TopBarHeight)) {
    Text(text = "Documentation", fontSize = 20.sp,
      modifier = Modifier.align(Alignment.Center), color = Color.White)
  }
}