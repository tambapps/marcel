package com.tambapps.marcel.android.marshell.ui.screen.shell.consult

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellViewModel
import com.tambapps.marcel.semantic.imprt.MutableImportResolver

@Composable
fun ImportsScreen(shellViewModel: ShellViewModel) {
  val imports = shellViewModel.imports
  Box(modifier = Modifier.fillMaxSize()) {
    if (imports == null || imports.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "No imports were used.", modifier = Modifier.align(Alignment.Center))
      }
    } else {
      ImportsLists(imports)
    }
  }
}

@Composable
private fun ImportsLists(imports: MutableImportResolver) {
  LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
    val headerModifier = Modifier.padding(bottom = 6.dp)
    val elementModifier = Modifier.padding(vertical = 2.dp)
    if (imports.staticMemberImports.isNotEmpty()) {
      item {
        Text(text = "Static members", style = MaterialTheme.typography.titleLarge, modifier = headerModifier)
      }
      for ((memberName, type) in imports.typeImports) {
        item {
          Text(text = "- ${type.className}.$memberName", modifier = elementModifier)
        }
      }
      item { Box(modifier = Modifier.padding(16.dp)) }
    }
    if (imports.typeImports.isNotEmpty()) {
      item {
        Text(text = "Types", style = MaterialTheme.typography.titleLarge, modifier = headerModifier)
      }
      for ((importedName, type) in imports.typeImports) {
        item {
          Text(text = "- " + if (importedName == type.simpleName) type.className else "${type.className} as $importedName", modifier = elementModifier)
        }
      }
      item { Box(modifier = Modifier.padding(16.dp)) }
    }

    if (imports.wildcardTypeImportPrefixes.isNotEmpty()) {
      item {
        Text(text = "Wildcard", style = MaterialTheme.typography.titleLarge, modifier = headerModifier)
      }
      items(imports.wildcardTypeImportPrefixes.sorted()) { prefix ->
        Text(text = "- $prefix.*", modifier = elementModifier)
      }
    }
  }
}