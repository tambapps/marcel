package com.tambapps.marcel.android.marshell.ui.screen.work

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkInfo
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.ui.component.ExpandableCard
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import com.tambapps.marcel.android.marshell.util.TimeUtils
import java.time.Duration

@Composable
fun WorkScriptCard(viewModel: ScriptCardViewModel, readOnly: Boolean = false) {
  val tintColor = if (viewModel.scriptTextError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
  ExpandableCard(expanded = viewModel.scriptCardExpanded,
    title = "Script",
    titleColor = tintColor,
    logoTint = tintColor,
    additionalLogos = if (readOnly) null else {
      {
        val context = LocalContext.current
        val pickPictureLauncher = rememberLauncherForActivityResult(
          ActivityResultContracts.GetContent()
        ) { imageUri ->
          viewModel.loadScript(context, imageUri)
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
      }
    }) {
    ScriptField(viewModel = viewModel, readOnly = readOnly, modifier = Modifier.weight(1f).padding(top = 8.dp))
  }
}

@Composable
fun ScriptField(viewModel: ScriptCardViewModel, readOnly: Boolean, modifier: Modifier) {
  var linesText by remember { mutableIntStateOf(1) }
  val style = remember { shellTextStyle.copy(lineHeight = 26.sp) }

  val linesTextScroll = rememberScrollState()
  val scriptTextScroll = rememberScrollState()

  // TODO answer to https://stackoverflow.com/questions/76655920/how-to-add-line-numbers-to-basictextfield-in-jetpack-compose
  //   with this code example
  // synchronize scrolling
  LaunchedEffect(linesTextScroll.value) {
    scriptTextScroll.scrollTo(linesTextScroll.value)
  }
  LaunchedEffect(scriptTextScroll.value) {
    linesTextScroll.scrollTo(scriptTextScroll.value)
  }

  Row(modifier = modifier) {
    BasicTextField(
      modifier = Modifier
        .fillMaxHeight()
        .width(12.dp * linesText.toString().length)
        .verticalScroll(linesTextScroll),
      value = IntRange(1, linesText).joinToString(separator = "\n"),
      readOnly = true,
      textStyle = style.copy(textAlign = TextAlign.End),
      onValueChange = {})

    VerticalDivider(
      modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
      color = Color.White
    )
    BasicTextField(
      modifier = Modifier
        .fillMaxHeight()
        .weight(1f)
        // this is a hack to prevent this https://stackoverflow.com/questions/76287857/when-parent-of-textfield-is-clickable-hardware-enter-return-button-triggers-its
        .onKeyEvent { it.type == KeyEventType.KeyUp && it.key == Key.Enter }
        .verticalScroll(scriptTextScroll),
      value = viewModel.scriptTextInput,
      readOnly = readOnly,
      textStyle = style,
      onValueChange = { textFieldValue ->
        val nbLines = textFieldValue.annotatedString.count { it == '\n' } + 1
        if (nbLines != linesText) linesText = nbLines
        viewModel.onScriptTextChange(textFieldValue)
      },
      visualTransformation = viewModel,
    )
  }
  if (viewModel.scriptTextError != null) {
    Text(
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
      text = viewModel.scriptTextError!!,
      color = MaterialTheme.colorScheme.error
    )
  }
}

@Composable
fun WorkStateText(shellWork: ShellWork, modifier: Modifier = Modifier, fontSize: TextUnit = TextUnit.Unspecified) {
  Text(text = stateText(shellWork), color = stateColor(shellWork), modifier = modifier, textAlign = TextAlign.Center, fontSize = fontSize)
}

fun runtimeText(work: ShellWork) = when {
  work.startTime != null ->
    if (work.isFinished) "ran ${TimeUtils.smartToString(work.startTime)} for ${
      TimeUtils.humanReadableFormat(
        Duration.between(work.startTime, work.endTime))}"
    else if (work.isPeriodic) when {
      work.state == WorkInfo.State.RUNNING -> "started ${TimeUtils.smartToString(work.startTime)}"
      work.endTime != null -> "last ran ${TimeUtils.smartToString(work.startTime)} for ${
        TimeUtils.humanReadableFormat(
          Duration.between(work.startTime, work.endTime))}"
      else -> "has not ran yet"
    }
    else "started " + TimeUtils.smartToString(work.startTime)
  work.scheduledAt != null -> "Scheduled for " + TimeUtils.smartToString(work.scheduledAt)
  work.failedReason == null -> "has not ran yet"
  else -> "has not ran yet"
}

private val Orange = Color(0xFFFFA500)
private val SkyBlue = Color(0xFF87CEEB)

private fun stateText(work: ShellWork): String {
  return if (work.period != null && !work.state.isFinished) {
    val unitStr = work.period.unit.name.lowercase()
    if (work.state == WorkInfo.State.RUNNING) "RUNNING"
    else if (work.period.amount == 1) "PERIODIC\n(every ${unitStr.removeSuffix("s")})"
    else "PERIODIC\n(every ${work.period.amount} $unitStr)"
  } else work.state.name
}

private fun stateColor(work: ShellWork) = when {
  work.state == WorkInfo.State.SUCCEEDED -> Color.Green
  work.state == WorkInfo.State.CANCELLED -> Orange
  work.state == WorkInfo.State.FAILED -> Color.Red
  work.isPeriodic || work.state == WorkInfo.State.RUNNING -> SkyBlue
  else -> Color.White
}