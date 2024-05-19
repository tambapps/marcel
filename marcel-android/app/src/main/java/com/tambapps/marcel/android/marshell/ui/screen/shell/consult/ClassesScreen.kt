package com.tambapps.marcel.android.marshell.ui.screen.shell.consult

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tambapps.marcel.android.marshell.ui.component.ExpandableCard
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellViewModel
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaType
import java.util.Optional

@Composable
internal fun ClassesScreen(shellViewModel: ShellViewModel) {
  val classes = listOf(Optional::class.javaType) // TODO fetch real types
  val symbolResolver = shellViewModel.symbolResolver
  Box(modifier = Modifier.fillMaxSize()) {
    if (symbolResolver == null || classes.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "No classes were defined in this session.", modifier = Modifier.align(Alignment.Center))
      }
    } else {
      ClassesList(symbolResolver, classes)
    }
  }
}

@Composable
private fun ClassesList(symbolResolver: ReplMarcelSymbolResolver, classes: List<JavaType>) {
  LazyColumn {
    items(classes) { type ->
      ExpandableCard(
        expanded = remember { mutableStateOf(false) }, 
        title = type.simpleName
      ) {
        Text(text = "Fields", style = MaterialTheme.typography.titleLarge)
        val fields = symbolResolver.getDeclaredFields(type)
        if (fields.isEmpty()) {
          Text(text = "No fields")
        } else {
          for (field in fields) {
            Text(text = "${field.type.simpleName} ${field.name}")
          }
        }

        Box(modifier = Modifier.padding(8.dp))
        Text(text = "Methods", style = MaterialTheme.typography.titleLarge)
        val methods = symbolResolver.getMethods(type)
        if (methods.isEmpty()) {
          Text(text = "No methods")
        } else {
          for (method in methods) {
            Text(text = method.toString(true))
          }
        }
      }
    }
  }
}