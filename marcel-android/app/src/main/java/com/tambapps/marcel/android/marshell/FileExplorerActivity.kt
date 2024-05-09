package com.tambapps.marcel.android.marshell

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.screen.settings.askManageFilePermission
import com.tambapps.marcel.android.marshell.ui.theme.MarcelAndroidTheme
import com.tambapps.marcel.android.marshell.ui.theme.TopBarIconSize
import com.tambapps.marcel.android.marshell.util.LifecycleStateListenerEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

class FileExplorerActivity : ComponentActivity() {


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      val viewModel: FileExplorerViewModel = viewModel()
      val context = LocalContext.current
      MarcelAndroidTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          LifecycleStateListenerEffect(
            onResume = viewModel::refresh
          )
          if (!viewModel.canManageFiles) {
            AlertDialog(
              title = {
                Text(text = "File access permission")
              },
              text = {
                Text(text = "To continue, please allow the app to access files of your device")
              },
              onDismissRequest = { /*TODO*/ },
              confirmButton = {
                TextButton(onClick = { askManageFilePermission(context) }) {
                  Text(text = "Allow")
                }
              }
            )
          } else {
            FileExplorerScreen(
              viewModel = viewModel,
              backPressedDispatcher = onBackPressedDispatcher
            )
          }
        }
      }
    }
  }
}

@Composable
fun FileExplorerScreen(
  viewModel: FileExplorerViewModel,
  backPressedDispatcher: OnBackPressedDispatcher
) {
  Column(Modifier.fillMaxSize()) {
    val currentDir = viewModel.currentDir
    TopBar(viewModel = viewModel, backPressedDispatcher = backPressedDispatcher)
    if (currentDir == null) return@Column
    FilePathRow(viewModel, currentDir)

  }
}

@Composable
fun TopBar(viewModel: FileExplorerViewModel, backPressedDispatcher: OnBackPressedDispatcher) {
  TopBarLayout {
    IconButton(
      modifier = Modifier.size(TopBarIconSize),
      onClick = { backPressedDispatcher.onBackPressed() },
    ) {
      Icon(
        modifier = Modifier.size(TopBarIconSize),
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = null,
        tint = Color.White
      )
    }
    if (viewModel.currentDir != null) {
      Text(text = viewModel.fileName(viewModel.currentDir!!), fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
  }
}

@Composable
fun FilePathRow(viewModel: FileExplorerViewModel, currentDir: File) {
  LazyRow(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    val path = currentDir.path.split("/")
    itemsIndexed(path) { i, dirName ->
      Text(text = dirName)
      if (i < path.lastIndex) {
        Text(text = ">")
      }
    }
  }
}

class FileExplorerViewModel: ViewModel() {
  var canManageFiles by mutableStateOf(Environment.isExternalStorageManager())
  var currentDir by mutableStateOf<File?>(null)

  fun refresh() {
    canManageFiles = Environment.isExternalStorageManager()
    if (canManageFiles && currentDir == null) {
      currentDir = Environment.getExternalStorageDirectory()
    }
  }

  fun fileName(f: File) = if (f == Environment.getExternalStorageDirectory()) "Internal Storage" else f.name
}