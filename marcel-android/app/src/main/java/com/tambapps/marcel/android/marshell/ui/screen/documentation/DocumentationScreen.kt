package com.tambapps.marcel.android.marshell.ui.screen.documentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tambapps.marcel.android.marshell.ui.component.MarkdownComposer
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight

@Composable
fun DocumentationScreen(viewModel: DocumentationViewModel = hiltViewModel()) {
  LaunchedEffect(Unit) {
    viewModel.fetchPage()
  }
  Column(modifier = Modifier
    .fillMaxSize()
    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
    .verticalScroll(rememberScrollState())) {
    TopBar()
    viewModel.node?.let {
      MarkdownComposer().Markdown(it)
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