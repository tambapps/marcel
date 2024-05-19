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
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType

@Composable
internal fun ClassesScreen(shellViewModel: ShellViewModel) {
  val classes = shellViewModel.definedTypes
  val symbolResolver = shellViewModel.symbolResolver
  Box(modifier = Modifier.fillMaxSize()) {
    if (symbolResolver == null || classes.isNullOrEmpty()) {
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
        val fields = symbolResolver.getDeclaredFields(type).sortedWith(compareBy({ it.name }, { it.type.className }))
        if (fields.isEmpty()) {
          Text(text = "No fields")
        } else {
          for (field in fields) {
            Text(text = "${field.type.simpleName} ${field.name}")
          }
        }

        Box(modifier = Modifier.padding(8.dp))
        Text(text = "Methods", style = MaterialTheme.typography.titleLarge)
        val methods = symbolResolver.getDeclaredMethods(type).sortedWith(compareBy({ it.name }, { it.parameters.size }))
        if (methods.isEmpty()) {
          Text(text = "No methods")
        } else {
          for (method in methods) {
            Text(text = method.signature)
          }
        }
      }
    }
  }
}

private val JavaMethod.signature get() = StringBuilder().apply {
  if (visibility != Visibility.PUBLIC) {
    append(visibility.name.lowercase())
    append(" ")
  }
  if (isConstructor) {
    append(ownerClass.simpleName)
  } else {
    if (isAbstract) append("abstract ")
    if (isStatic) append("static ")
    if (isAsync) append("async ")
    append("fun ")
    append(returnType.simpleName)
    append(" ")
    append(name)
  }
  append(parameters.joinToString(separator = ",", prefix = "(", postfix = ")", transform = { "${it.type.simpleName} ${it.name}" }))
}.toString()