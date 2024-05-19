package com.tambapps.marcel.android.marshell.ui.screen.shell.consult

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellViewModel

@Composable
internal fun VariablesScreen(shellViewModel: ShellViewModel) {
  val variables: MutableMap<String, Any?>? = shellViewModel.binding?.variables
  Box(modifier = Modifier.fillMaxSize()) {
    if (variables.isNullOrEmpty()) {
      Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "No variables are defined.", modifier = Modifier.align(Alignment.Center))
      }
    } else {
      VariableTable(variables)
    }
  }
}

@Composable
private fun VariableTable(variables: MutableMap<String, Any?>) {
  variables.entries.toList()
  LazyColumn {
    item {
      HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
      VariableTableRow("Name", "Value", clickable = false)
      HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
    }
    for ((name, value) in variables) {
      item {
        val type = (value?.javaClass ?: java.lang.Object::class.java).name
        VariableTableRow(name, value, dialogText = "Type: $type\nValue: $value")
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
      }
    }
  }
}