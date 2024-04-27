package com.tambapps.marcel.android.marshell.ui.screen.shell

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.theme.TopBarIconSize
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import marcel.lang.util.MarcelVersion
import java.io.IOException
import java.io.InputStream

val HEADER = "Marshell (Marcel: ${MarcelVersion.VERSION}, Android ${Build.VERSION.RELEASE})"

@Composable
fun ShellScreen(
  shellSessionFactory: ShellSessionFactory,
  scope: CoroutineScope = rememberCoroutineScope(),
  viewModel: ShellViewModel = viewModel(
    factory = ShellViewModelFactory(shellSessionFactory)
  )
) {
  Column(modifier = Modifier.fillMaxSize()) {
    TopBar(viewModel)
    val listState = rememberLazyListState()
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(), state = listState
    ) {
      item {
        HistoryText(text = HEADER)
      }
      viewModel.prompts.forEach { prompt: Prompt ->
        item {
          if (prompt.type == Prompt.Type.INPUT) {
            Row {
              HistoryText(
                text = "> ", color = Color.White,
                padding = PaddingValues(top = 16.dp)
              )
              HistoryText(
                text = prompt.text, color = Color.White,
                padding = PaddingValues(top = 16.dp)
              )
            }
          } else {
            HistoryText(
              text = prompt.text, color = when (prompt.type) {
                Prompt.Type.INPUT, Prompt.Type.STDOUT -> Color.White
                Prompt.Type.SUCCESS_OUTPUT -> Color.Green
                Prompt.Type.ERROR_OUTPUT -> Color.Red
              },
              padding = PaddingValues(top = 8.dp)
            )

          }
        }
      }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      OutlinedTextField(
        value = viewModel.textInput.value,
        onValueChange = {
          // TODO something weird. called too many times
          viewModel.textInput.value = it
          viewModel.highlightTextInput()
        },
        textStyle = shellTextStyle,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(36.dp)
      )
      PromptButton(viewModel, scope, listState)
    }
  }
}

@Composable
fun TopBar(viewModel: ShellViewModel) {
  val context = LocalContext.current
  TopBarLayout(horizontalArrangement = Arrangement.End) {
    ShellIconButton(
      modifier = shellIconModifier(4.dp),
      onClick = { Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show() },
      drawable = R.drawable.dumbell,
      contentDescription = "handle dumbbells"
    )

    Box(modifier = Modifier.width(10.dp))

    ShellIconButton(
      modifier = shellIconModifier(3.dp),
      onClick = { Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show() },
      drawable = R.drawable.view,
      contentDescription = "view shell functions/variables"
    )

    Box(modifier = Modifier.width(10.dp))

    ShellIconButton(
      modifier = shellIconModifier(),
      onClick = { Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show() },
      drawable = R.drawable.save,
      enabled = viewModel.prompts.any { it.type == Prompt.Type.INPUT },
      contentDescription = "save session to file"
    )

    Box(modifier = Modifier.width(10.dp))

    val pickPictureLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.GetContent()
    ) { imageUri ->
      if (imageUri != null) {
        val result = readText(context.contentResolver.openInputStream(imageUri))
        if (result.isFailure) {
          Toast.makeText(context, "Error: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_SHORT).show()
          return@rememberLauncherForActivityResult
        }
        viewModel.highlightTextInput(result.getOrNull()!!)
      }
    }
    ShellIconButton(
      modifier = shellIconModifier(),
      onClick = {
        pickPictureLauncher.launch("*/*")
      },
      drawable = R.drawable.downloads,
      contentDescription = "import script"
    )

    Box(modifier = Modifier.width(10.dp))

    ShellIconButton(
      modifier = shellIconModifier(),
      onClick = { viewModel.historyUp() },
      drawable = R.drawable.navigate_up_arrow,
      contentDescription = "navigate up"
    )

    Box(modifier = Modifier.width(4.dp))

    ShellIconButton(
      modifier = shellIconModifier().rotate(180f),
      onClick = { viewModel.historyDown() },
      drawable = R.drawable.navigate_up_arrow,
      contentDescription = "navigate down"
    )
  }
}

private fun shellIconModifier(horizontalPadding: Dp = 6.dp) = Modifier
  .size(TopBarIconSize)
  .padding(horizontal = horizontalPadding)

private fun readText(inputStream: InputStream?): Result<String> {
  if (inputStream == null) {
    return Result.failure(IOException("Couldn't open file"))
  }
  return try {
    Result.success(inputStream.reader().use { it.readText() })
  } catch (e: IOException) {
    Result.failure(e)
  }
}

@Composable
private fun ShellIconButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  enabled: Boolean = true,
  drawable: Int,
  contentDescription: String
) {
  IconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
  ) {
    Icon(
      painter = painterResource(id = drawable),
      contentDescription = contentDescription,
      tint = if (enabled) Color.White else Color.Gray
    )
  }
}

@Composable
fun HistoryText(
  text: String,
  color: Color? = null,
  padding: PaddingValues = PaddingValues(all = 0.dp)
) {
  Text(
    modifier = Modifier.padding(padding),
    text = text,
    style = color?.let { shellTextStyle.copy(color = it) } ?: shellTextStyle,
  )
}

@Composable
fun PromptButton(viewModel: ShellViewModel, scope: CoroutineScope, listState: LazyListState) {
  IconButton(
    colors = IconButtonDefaults.iconButtonColors()
      .copy(containerColor = Color.White, disabledContainerColor = Color.Gray),
    enabled = !viewModel.isEvaluating.value,
    onClick = {
      val input = viewModel.textInput.value.text.trim()
      if (input.isNotBlank()) {
        viewModel.prompt(input)
        scope.launch { listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1) }
      }
    },
  ) {
    Image(
      painter = painterResource(id = R.drawable.prompt),
      contentDescription = null,
      colorFilter = ColorFilter.tint(Color.Black),
      modifier = Modifier.fillMaxSize(fraction = 0.5f)
    )
  }
}