package com.tambapps.marcel.android.marshell.ui.screen.work.create

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.ui.screen.shell.readText
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle

@Composable
fun WorkCreateScreen(viewModel: WorkCreateViewModel) {
  Box(modifier = Modifier
    .fillMaxSize(),
    contentAlignment = Alignment.BottomEnd) {
    Column(modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 8.dp)) {
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
  Box(modifier = Modifier.padding(8.dp))

  ExpandableCard(viewModel = viewModel, title = "Script") {
    TextField(
      modifier = Modifier.fillMaxWidth(),
      value = viewModel.scriptTextInput,
      onValueChange = { viewModel.scriptTextInput = it },
      visualTransformation = viewModel
      )
  }
  Box(modifier = Modifier.padding(8.dp))
  TextIconButton(fieldName = "Period", value = "One-shot", onClick = { /*TODO*/ })
  TextIconButton(fieldName = "Schedule", value = "Run now", onClick = { /*TODO*/ })
  TextIconButton(fieldName = "Requires Network?", value = if (viewModel.requiresNetwork) "Yes" else "No", onClick = { viewModel.requiresNetwork = !viewModel.requiresNetwork })
  TextIconButton(fieldName = "Run silently?", value = if (viewModel.silent) "Yes" else "No", onClick = { viewModel.silent = !viewModel.silent })
}

@Composable
private fun TextIconButton(fieldName: String, value: String, onClick: () -> Unit) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(horizontal = 8.dp)
  ) {
    Text(
      modifier = Modifier.width(100.dp),
      text = fieldName,
      style = shellTextStyle,
      color = MaterialTheme.colorScheme.primary
    )

    Text(
      modifier = Modifier
        .width(100.dp)
        .padding(horizontal = 8.dp),
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

@Composable
fun ExpandableCard(
  viewModel: WorkCreateViewModel,
  title: String,
  expandedContent: @Composable () -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val rotation by animateFloatAsState(
    targetValue = if (expanded) 180f else 0f, label = "Arrow Animation"
  )
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .animateContentSize(
        animationSpec = tween(
          durationMillis = 300,
          easing = LinearOutSlowInEasing
        )
      ),
    onClick = {
      expanded = !expanded
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          modifier = Modifier
            .weight(6f),
          text = title,
          style = shellTextStyle,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1,
        )

        val context = LocalContext.current
        val pickPictureLauncher = rememberLauncherForActivityResult(
          ActivityResultContracts.GetContent()
        ) { imageUri ->
          if (imageUri != null) {
            val result = readText(context.contentResolver.openInputStream(imageUri))
            if (result.isFailure) {
              Toast.makeText(context, "Error: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_SHORT).show()
              return@rememberLauncherForActivityResult
            }
            viewModel.setScriptTextInput(result.getOrNull()!!)
            expanded = true
          }
        }
        IconButton(
          modifier = Modifier.weight(1f)
            .size(24.dp),
          onClick = { pickPictureLauncher.launch("*/*") }) {
          Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.folder),
            contentDescription = "Pick file",
            tint = MaterialTheme.colorScheme.primary,
          )
        }

        IconButton(
          modifier = Modifier
            .weight(1f)
            .rotate(rotation)
            .size(36.dp),
          onClick = {
            expanded = !expanded
          }) {
          Icon(
            modifier = Modifier.size(50.dp),
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Drop-Down Arrow",
            tint = MaterialTheme.colorScheme.primary,
            )
        }
      }
      if (expanded) {
        expandedContent.invoke()
      }
    }

  }
}