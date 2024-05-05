package com.tambapps.marcel.android.marshell.ui.screen.work.create

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
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
      onClick = { viewModel.validateAndSave() }
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
    singleLine = true,
    onValueChange = viewModel::onNameChange,
    label = { Text("Name") },
    supportingText = viewModel.nameError?.let { error -> {
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = error,
        color = MaterialTheme.colorScheme.error
      )
    }},
    isError = viewModel.nameError != null
  )

  OutlinedTextField(
    modifier = Modifier.padding(vertical = 8.dp),
    value = viewModel.description,
    singleLine = true,
    onValueChange = { viewModel.description = it },
    label = { Text("Description (optional)") }
  )
  Box(modifier = Modifier.padding(8.dp))

  ExpandableCard(viewModel = viewModel, title = "Script") {
    // TODO find a way to add line numbers. same on editor screen
    TextField(
      // this is a hack to prevent this https://stackoverflow.com/questions/76287857/when-parent-of-textfield-is-clickable-hardware-enter-return-button-triggers-its
      modifier = Modifier.fillMaxWidth().onKeyEvent { it.type == KeyEventType.KeyUp && it.key == Key.Enter },
      value = viewModel.scriptTextInput,
      onValueChange = viewModel::onScriptTextChange,
      visualTransformation = viewModel,
      isError = viewModel.scriptTextError != null,
      supportingText = viewModel.scriptTextError?.let { error -> {
        Text(
          modifier = Modifier.fillMaxWidth(),
          text = error,
          color = MaterialTheme.colorScheme.error
        )
      }},
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
  val rotation by animateFloatAsState(
    targetValue = if (viewModel.scriptCardExpanded) 180f else 0f, label = "Arrow Animation"
  )
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .animateContentSize(
        animationSpec = tween(
          durationMillis = 300,
          easing = LinearOutSlowInEasing
        )
      )
      .clickable { viewModel.scriptCardExpanded = !viewModel.scriptCardExpanded },
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
            viewModel.scriptCardExpanded = true
          }
        }
        IconButton(
          modifier = Modifier
            .weight(1f)
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
          onClick = { viewModel.scriptCardExpanded = !viewModel.scriptCardExpanded }) {
          Icon(
            modifier = Modifier.size(50.dp),
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Drop-Down Arrow",
            tint = MaterialTheme.colorScheme.primary,
            )
        }
      }
      if (viewModel.scriptCardExpanded) {
        expandedContent.invoke()
      }
    }

  }
}