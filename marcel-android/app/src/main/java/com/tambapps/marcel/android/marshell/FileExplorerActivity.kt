package com.tambapps.marcel.android.marshell

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.screen.settings.askManageFilePermission
import com.tambapps.marcel.android.marshell.ui.screen.settings.paddingStart
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
    FilePathRow(viewModel)
    LazyColumn(
      Modifier
        .weight(1f)
        .fillMaxWidth()) {
      items(viewModel.children) { file ->
        Row(
          modifier = Modifier
            .clickable { viewModel.move(file) }
            .height(60.dp)
            .fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(modifier = Modifier.size(60.dp)) {
            if (file.isDirectory) {
              Icon(
                modifier = Modifier.size(35.dp).align(Alignment.Center),
                painter = painterResource(id = R.drawable.folder),
                contentDescription = "folder",
                tint = Color.White,
              )
            }
          }
          Text(text = file.name)
        }
      }
    }

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
fun FilePathRow(viewModel: FileExplorerViewModel) {
  val state = rememberLazyListState()

  LaunchedEffect(viewModel.filesPath.size) {
    // make sure to scroll to the end each time a new item is added on the prompts list
    state.scrollToItem(state.layoutInfo.totalItemsCount - 1)
  }
  LazyRow(
    state = state,
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    itemsIndexed(viewModel.filesPath) { i, file ->
      TextButton(onClick = { viewModel.move(file) }) {
        Text(text = viewModel.fileName(file), color = Color.White, fontWeight = FontWeight.Normal)
      }
      if (i < viewModel.filesPath.lastIndex) {
        Text(text = ">")
      }
    }
  }
}

class FileExplorerViewModel: ViewModel() {
  var canManageFiles by mutableStateOf(Environment.isExternalStorageManager())
  var currentDir by mutableStateOf<File?>(null)
  val filesPath = mutableStateListOf<File>()
  val children = mutableStateListOf<File>()

  fun move(file: File) {
    currentDir = file
    update(file)
  }

  fun refresh() {
    canManageFiles = Environment.isExternalStorageManager()
    if (canManageFiles && currentDir == null) {
      currentDir = Environment.getExternalStorageDirectory()
    }
    currentDir?.let(this::update)
  }

  private fun update(currentDir: File) {
    filesPath.clear()
    var f: File? = currentDir
    while (f != Environment.getExternalStorageDirectory() && f != null) {
      filesPath.add(f)
      f = f.parentFile
    }
    filesPath.add(Environment.getExternalStorageDirectory())
    filesPath.reverse()

    children.clear()
    currentDir.listFiles()?.let {
      // TODO make sorting criteria configurable
      it.sortBy { child -> child.name }
      children.addAll(it)
    }
  }

  fun fileName(f: File) = if (f == Environment.getExternalStorageDirectory()) "Internal Storage" else f.name
}