package com.tambapps.marcel.android.marshell.ui.screen.shell.consult

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun VariableTableRow(
  name: String,
  value: Any?,
  clickable: Boolean = true,
  dialogText: String? = null,
  onDelete: (() -> Unit)? = null,
) {
  val valueStr = java.lang.String.valueOf(value)
  var modifier = Modifier
    .fillMaxWidth()
    .height(50.dp)
  if (clickable) {
    var showDialog by remember { mutableStateOf(false) }
    modifier = modifier.clickable { showDialog = true }
    if (showDialog) {
      AlertDialog(
        onDismissRequest = { showDialog = false },
        title = {
          Text(text = name, overflow = TextOverflow.Ellipsis)
        },
        text = {
          Text(text = dialogText ?: valueStr, modifier = modifier.verticalScroll(rememberScrollState()))
        },
        confirmButton = {
          if (onDelete != null) {
            TextButton(onClick = {
              onDelete.invoke()
              showDialog = false
            }) {
              Text(text = "Remove", color = Color.Red)
            }
          }
          TextButton(onClick = { showDialog = false }) {
            Text(text = "OK")
          }
        })
    }
  }
  Row(modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    VerticalDivider(color = MaterialTheme.colorScheme.onBackground)
    TableText(name)
    VerticalDivider(color = MaterialTheme.colorScheme.onBackground)
    TableText(valueStr)
    VerticalDivider(color = MaterialTheme.colorScheme.onBackground)
  }
}

@Composable
private fun RowScope.TableText(text: String) = Text(
  text = text,
  modifier = Modifier.weight(1f),
  textAlign = TextAlign.Center,
  overflow = TextOverflow.Ellipsis
)