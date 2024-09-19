package com.tambapps.marcel.android.marshell.ui.screen.shell.consult

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellViewModel
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode

@Composable
internal fun FunctionsScreen(shellViewModel: ShellViewModel) {
  val functions = remember {
    mutableStateListOf<MethodCstNode>().apply {
      if (shellViewModel.functions != null) {
        addAll(shellViewModel.functions!!)
      }
      sortBy { it.name }
    }
  }
  Box(modifier = Modifier.fillMaxSize()) {
    if (functions.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "No functions are defined.", modifier = Modifier.align(Alignment.Center))
      }
    } else {
      FunctionTable(shellViewModel, functions)
    }
  }
}

@Composable
private fun FunctionTable(shellViewModel: ShellViewModel, functions: MutableList<MethodCstNode>) {
  LazyColumn {
    item {
      HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
      VariableTableRow("Name", "Signature", clickable = false)
      HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
    }
    items(functions) { method ->
      VariableTableRow(method.name, method.signature,
        onDelete = {
          shellViewModel.removeMethod(method)
          functions.remove(method) // needed in order for the view to recompose
        })
      HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
    }
  }
}

private val MethodCstNode.signature: String get() =
      parameters.joinToString(prefix = "(", separator = ", ", transform = { "${it.type.simpleName} ${it.name}" }, postfix = ") -> ${returnTypeNode.simpleName}")
private val TypeCstNode.simpleName: String get() {
  val dotIndex = value.indexOf('.')
  return if (dotIndex > 0) value.substring(dotIndex + 1) else value
}