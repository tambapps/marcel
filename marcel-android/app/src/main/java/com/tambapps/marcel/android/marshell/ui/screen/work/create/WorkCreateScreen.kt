package com.tambapps.marcel.android.marshell.ui.screen.work.create

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tambapps.marcel.android.marshell.Routes
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriod
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriodUnit
import com.tambapps.marcel.android.marshell.ui.component.EXPANDABLE_CARD_ANIMATION_SPEC
import com.tambapps.marcel.android.marshell.ui.component.PickerExample
import com.tambapps.marcel.android.marshell.ui.screen.work.WorkScriptCard
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun WorkCreateScreen(
  viewModel: WorkCreateViewModel,
  navController: NavController
) {
  val context = LocalContext.current
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
        viewModel.validateAndSave(context) {
          // navigate back to works list and force the screen to reload
          navController.navigate(Routes.WORK_LIST) {
            popUpTo(Routes.WORK_LIST) { inclusive = true }
          }
        }
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

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

@Composable
private fun Form(viewModel: WorkCreateViewModel) {
  Box(modifier = Modifier
    .fillMaxWidth()
    .animateContentSize(animationSpec = EXPANDABLE_CARD_ANIMATION_SPEC)
  ) {
    if (viewModel.scriptCardExpanded.value) {
      return@Box
    }
    Column {
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
    }
    Box(modifier = Modifier.padding(8.dp))
  }

  WorkScriptCard(viewModel = viewModel)
  Box(modifier = Modifier.padding(8.dp))

  val showSchedulePickerDialog = remember { mutableStateOf(false) }
  TextIconButton(
    fieldName = "Schedule for",
    value = if (viewModel.scheduleAt == null) "now" else DATE_FORMATTER.format(viewModel.scheduleAt),
    onClick = { showSchedulePickerDialog.value = true }
  )
  DateTimePickerDialog(
    viewModel = viewModel,
    show = showSchedulePickerDialog,
    onDismissRequest = { showSchedulePickerDialog.value = false }
  )

  val showPeriodDialog = remember { mutableStateOf(false) }
  TextIconButton(fieldName = "Period", value = if (viewModel.period == null) "One-shot" else "${viewModel.period?.amount} ${viewModel.period?.unit?.name?.lowercase()}", onClick = { showPeriodDialog.value = true })
  PeriodPickerDialog(
    viewModel = viewModel,
    show = showPeriodDialog,
    onDismissRequest = { showPeriodDialog.value = false }
  )

  TextIconButton(fieldName = "Requires Network?", value = if (viewModel.requiresNetwork) "Yes" else "No", onClick = { viewModel.requiresNetwork = !viewModel.requiresNetwork })
  TextIconButton(fieldName = "Run silently?", value = if (viewModel.silent) "Yes" else "No", onClick = { viewModel.silent = !viewModel.silent })
}

@OptIn(ExperimentalMaterial3Api::class)
private class TodayOrAfter: SelectableDates {
  val nowYear = LocalDate.now().year
  val nowTimestamp = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()

  override fun isSelectableDate(utcTimeMillis: Long): Boolean {
    return utcTimeMillis >= nowTimestamp
  }
  override fun isSelectableYear(year: Int): Boolean {
    return year >= nowYear
  }
}
@Composable
private fun PeriodPickerDialog(
  viewModel: WorkCreateViewModel,
  show: MutableState<Boolean>,
  onDismissRequest: () -> Unit
) {
  if (!show.value) {
    return
  }

  val amountState = remember { mutableIntStateOf(viewModel.period?.amount ?: 1) }
  val unitState = remember { mutableStateOf(viewModel.period?.unit ?: WorkPeriodUnit.HOURS) }
  val period = WorkPeriod(amountState.intValue, unitState.value)
  val isValidPeriod = period.toMinutes() in 15..43200
  AlertDialog(
    text = {
      Box(modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)) {
        PickerExample(
          amountState = amountState,
          unitState = unitState,
          footer = {

          }
        )
        if (!isValidPeriod) {
          Text(
            text = "Period must be greater than 15 minutes",
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(vertical = 16.dp),
            color = Color.Red
          )

        }
      }
    },
    onDismissRequest = onDismissRequest,
    dismissButton = {
      TextButton(onClick = {
        viewModel.period = null
        onDismissRequest.invoke()
      }) {
        Text("One-shot")
      }
    },
    confirmButton = {
      TextButton(onClick = {
         viewModel.period = period
        onDismissRequest.invoke()
      }, enabled = isValidPeriod) {
        Text("Confirm")
      }
    }
  )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ExposedDropdownMenu(
  values: List<T>,
  selectedItem: MutableState<T>
) {
  var dropDownExpanded by remember { mutableStateOf(false) }

  ExposedDropdownMenuBox(
    modifier = Modifier.width(150.dp),
    expanded = dropDownExpanded,
    onExpandedChange = {
      dropDownExpanded = !dropDownExpanded
    }
  ) {
    TextField(
      value = selectedItem.value.toString(),
      onValueChange = {},
      readOnly = true,
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded) },
      modifier = Modifier.menuAnchor()
    )

    ExposedDropdownMenu(
      expanded = dropDownExpanded,
      onDismissRequest = { dropDownExpanded = false }
    ) {

      for (item in values) {
        DropdownMenuItem(
          text = { Text(text = item.toString()) },
          onClick = {
            selectedItem.value = item
            dropDownExpanded = false
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePickerDialog(
  viewModel: WorkCreateViewModel,
  show: MutableState<Boolean>,
  onDismissRequest: () -> Unit
) {
  if (!show.value) {
    return
  }
  var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
  val datePickerState by remember { mutableStateOf(DatePickerState(
    locale = CalendarLocale.getDefault(),
    initialSelectedDateMillis = System.currentTimeMillis(),
    selectableDates = TodayOrAfter()
  )) }
  val timePickerState by remember {
    val now = LocalDateTime.now()
    mutableStateOf(TimePickerState(initialHour = now.hour, initialMinute = now.minute, is24Hour = true))
  }
  AlertDialog(
    text = {
      Column {
        if (selectedDate == null) {
          DatePicker(state = datePickerState)
        } else {
          Text(text = "Select time", modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 32.dp))
          TimePicker(state = timePickerState)
        }
      }
    },
    onDismissRequest = onDismissRequest,
    dismissButton = {
      TextButton(onClick = {
        if (selectedDate == null) {
          onDismissRequest.invoke()
        } else {
          selectedDate = null
        }
      }) {
        Text("Cancel")
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (selectedDate == null) {
            selectedDate = datePickerState.selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
          } else {
            val time = LocalDateTime.of(selectedDate!!, LocalTime.of(timePickerState.hour, timePickerState.minute))
            if (time.isBefore(LocalDateTime.now().plusMinutes(15))) {
              viewModel.scheduleAt = null // setting it to now
            } else {
              viewModel.scheduleAt = time
            }
            onDismissRequest.invoke()
          }
        }
      ) {
        Text(if (selectedDate == null) "Continue" else "Confirm")
      }
    }
  )
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
        .width(150.dp)
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
    text = "New Workout",
    style = shellTextStyle,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 16.dp)
      .height(TopBarHeight), textAlign = TextAlign.Center,
    fontSize = 22.sp
  )
}
