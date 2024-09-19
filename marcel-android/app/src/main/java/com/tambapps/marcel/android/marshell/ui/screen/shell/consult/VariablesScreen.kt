package com.tambapps.marcel.android.marshell.ui.screen.shell.consult

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellViewModel
import marcel.lang.Binding

@Composable
internal fun VariablesScreen(shellViewModel: ShellViewModel) {
  Box(modifier = Modifier.fillMaxSize()) {
    val binding = shellViewModel.binding
    val variables: MutableMap<String, Any?> = remember {
      mutableStateMapOf<String, Any?>().apply {
        if (binding != null) {
          putAll(binding.variables)
        }
      }
    }
    if (variables.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "No variables are defined.", modifier = Modifier.align(Alignment.Center))
      }
    } else {
      VariableTable(shellViewModel, variables)
    }
  }
}

@Composable
private fun VariableTable(
  shellViewModel: ShellViewModel,
  variables: MutableMap<String, Any?>
) {
  LazyColumn {
    item {
      HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
      VariableTableRow("Name", "Value", clickable = false)
      HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
    }
    for ((name, value) in variables) {
      item {
        val type = (value?.javaClass ?: java.lang.Object::class.java).name
        VariableTableRow(name, value, dialogText = "Type: $type\nValue: $value",
          onDelete = {
            shellViewModel.removeVariable(name)
            variables.remove(name) // needed in order for the view to recompose
          })
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
      }
    }
  }
}