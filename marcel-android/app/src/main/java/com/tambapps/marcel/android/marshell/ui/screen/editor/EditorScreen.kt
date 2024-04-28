package com.tambapps.marcel.android.marshell.ui.screen.editor

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.ui.component.TopBarIconButton
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.component.shellIconModifier


@Composable
fun EditorScreen(viewModel: EditorViewModel = viewModel()) {
  Column(modifier = Modifier.fillMaxSize()) {
    TopBar()
    Box(modifier = Modifier
      .weight(1f)
      .fillMaxWidth(),
      contentAlignment = Alignment.BottomEnd) {

      TextField(
        modifier = Modifier.fillMaxSize(),
        value = viewModel.textInput.value,
        onValueChange = { viewModel.textInput.value = it },
        /*
 TODO
visualTransformation = { text ->
  TransformedText(
    text = viewModel.highlight(text),
    offsetMapping = OffsetMapping.Identity
  )

        }
         */

      )
      FloatingActionButton(
        modifier = Modifier.padding(all = 16.dp),
        onClick = { /*TODO*/ }
      ) {
        Icon(
          modifier = Modifier.size(32.dp),
          painter = painterResource(id = R.drawable.save),
          contentDescription = "Save",
          tint = Color.White
        )
      }

    }
  }

}
@Composable
private fun TopBar() {
  val context = LocalContext.current
  TopBarLayout(horizontalArrangement = Arrangement.End) {
    TopBarIconButton(
      modifier = shellIconModifier(),
      onClick = { /*TODO*/ },
      drawable = R.drawable.folder,
      contentDescription = "Open file"
    )

  }
}