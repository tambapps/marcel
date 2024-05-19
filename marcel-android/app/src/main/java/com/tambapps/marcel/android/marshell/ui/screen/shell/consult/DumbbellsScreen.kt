package com.tambapps.marcel.android.marshell.ui.screen.shell.consult

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellViewModel
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle

@Composable
fun DumbbellsScreen(shellViewModel: ShellViewModel) {
  val dumbbells = shellViewModel.dumbbells?.sorted()
  Box(modifier = Modifier.fillMaxSize()) {
    if (dumbbells.isNullOrEmpty()) {
      Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "No imports were used.", modifier = Modifier.align(Alignment.Center))
      }
    } else {
      DumbbellsList(dumbbells)
    }
  }
}

@Composable
private fun DumbbellsList(dumbbells: List<String>) {
  LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
    items(dumbbells) { dumbbell ->
      Text(text = "- $dumbbell", style = MaterialTheme.typography.shellTextStyle, modifier = Modifier.padding(4.dp))
    }
  }
}