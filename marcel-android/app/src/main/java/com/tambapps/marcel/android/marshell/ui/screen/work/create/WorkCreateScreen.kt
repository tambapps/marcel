package com.tambapps.marcel.android.marshell.ui.screen.work.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tambapps.marcel.android.marshell.Routes
import com.tambapps.marcel.android.marshell.ui.component.CheckBoxText
import com.tambapps.marcel.android.marshell.ui.screen.work.list.ShellWorkItem
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle


@Composable
fun WorkCreateScreen(viewModel: WorkCreateViewModel) {
  Box(modifier = Modifier
    .fillMaxSize(),
    contentAlignment = Alignment.BottomEnd) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
      Header()
      Form(viewModel)
    }

    FloatingActionButton(
      modifier = Modifier.padding(all = 16.dp),
      onClick = {
        // TODO
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
private fun Form(viewModel: WorkCreateViewModel) {
  OutlinedTextField(
    value = viewModel.name,
    onValueChange = { viewModel.name = it },
    label = { Text("Name") }
  )

  OutlinedTextField(
    modifier = Modifier.padding(vertical = 8.dp),
    value = viewModel.description,
    onValueChange = { viewModel.description = it },
    label = { Text("Description (optional)") }
  )
  TextIconButton(fieldName = "Period", value = "One-shot", onClick = { /*TODO*/ })
  TextIconButton(fieldName = "Schedule", value = "Run now", onClick = { /*TODO*/ })
  TextIconButton(fieldName = "Requires Network?", value = if (viewModel.requiresNetwork) "Yes" else "No", onClick = { viewModel.requiresNetwork = !viewModel.requiresNetwork })
  TextIconButton(fieldName = "Run silently?", value = if (viewModel.silent) "Yes" else "No", onClick = { viewModel.silent = !viewModel.silent })
}

@Composable
private fun TextIconButton(fieldName: String, value: String, onClick: () -> Unit) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(
      modifier = Modifier.width(100.dp),
      text = fieldName,
      style = shellTextStyle,
      color = MaterialTheme.colorScheme.primary
    )

    Text(
      modifier = Modifier.width(100.dp).padding(horizontal = 8.dp),
      text = value,
      style = shellTextStyle,
      textAlign = TextAlign.Center
    )

    IconButton(onClick = onClick) {
      Icon(Icons.Filled.Edit, "edit", tint = Color.White)
    }
  }

}
@Composable
private fun Header() {
  // TODO write help button that opens a dialog and explain shell works to user
  Text(
    text = "New Work",
    style = shellTextStyle,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 16.dp)
      .height(TopBarHeight), textAlign = TextAlign.Center,
    fontSize = 22.sp
  )
}