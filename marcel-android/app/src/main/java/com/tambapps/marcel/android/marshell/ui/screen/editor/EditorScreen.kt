package com.tambapps.marcel.android.marshell.ui.screen.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tambapps.marcel.android.marshell.FilePickerActivity
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.ui.component.ScriptTextField
import com.tambapps.marcel.android.marshell.ui.component.TopBarIconButton
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.component.shellIconModifier


@Composable
fun EditorScreen(viewModel: EditorViewModel) {
  Column(modifier = Modifier.fillMaxSize()) {
    TopBar(viewModel)
    Box(modifier = Modifier
      .weight(1f)
      .fillMaxWidth(),
      contentAlignment = Alignment.BottomEnd) {

      ScriptTextField(
        viewModel = viewModel,
        modifier = Modifier.fillMaxSize(),
      )
      FloatingActionButton(
        modifier = Modifier.padding(all = 16.dp),
        onClick = { /*TODO*/ }
      ) {
        Icon(
          modifier = Modifier.size(23.dp),
          painter = painterResource(id = R.drawable.save),
          contentDescription = "Save",
          tint = Color.White
        )
      }

    }
  }

}
@Composable
private fun TopBar(viewModel: EditorViewModel) {
  val context = LocalContext.current
  val pickFileLauncher = FilePickerActivity.rememberFilePickerForActivityResult { file ->
    viewModel.loadScript(context, file)
  }
  TopBarLayout(horizontalArrangement = Arrangement.End) {
    TopBarIconButton(
      modifier = shellIconModifier(4.dp),
      onClick = { pickFileLauncher.launch(FilePickerActivity.Args()) },
      drawable = R.drawable.folder,
      contentDescription = "Open file"
    )

  }
}