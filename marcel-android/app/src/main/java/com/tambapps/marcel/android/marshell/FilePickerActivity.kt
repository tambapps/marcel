package com.tambapps.marcel.android.marshell

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.addCallback
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.tambapps.marcel.android.marshell.ui.theme.MarcelAndroidTheme
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight
import com.tambapps.marcel.android.marshell.ui.theme.TopBarIconSize
import com.tambapps.marcel.android.marshell.util.LifecycleStateListenerEffect
import java.io.File

class FilePickerActivity : ComponentActivity() {

  companion object {
    val SCRIPT_FILE_EXTENSIONS = listOf(".mcl", ".txt", ".marcel")
    const val PICKED_FILE_PATH_KEY = "pfpk"
    const val ALLOWED_FILE_EXTENSIONS_KEY = "afek"
    const val ALLOW_CREATE_FILE_KEY = "acfilek"
    const val DIRECTORY_ONLY_KEY = "pick_directoryk"
    const val START_DIRECTORY_KEY = "start_directory"

    @Composable
    fun rememberFilePickerForActivityResult(callback: (File?) -> Unit): ManagedActivityResultLauncher<Args, File?> {
      return rememberLauncherForActivityResult(Contract(), callback)
    }
  }

  data class Args(
    val pickDirectory: Boolean = false,
    val allowedFileExtensions: List<String>? = SCRIPT_FILE_EXTENSIONS,
    val allowCreateNewFile: Boolean = false
  )
  class Contract: ActivityResultContract<Args, File?>() {
    override fun createIntent(context: Context, input: Args) = Intent(context, FilePickerActivity::class.java).apply {
      if (input.pickDirectory) putExtra(DIRECTORY_ONLY_KEY, true)
      if (input.allowCreateNewFile) putExtra(ALLOW_CREATE_FILE_KEY, true)
      input.allowedFileExtensions?.let {
        putExtra(ALLOWED_FILE_EXTENSIONS_KEY, it.toTypedArray())
      }
    }

    override fun parseResult(resultCode: Int, intent: Intent?)
        = if (resultCode == RESULT_OK && intent != null) File(intent.getStringExtra(PICKED_FILE_PATH_KEY)!!)
    else null
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      val viewModel: FileExplorerViewModel = viewModel()
      onBackPressedDispatcher.addCallback {
        val parentFile = viewModel.currentDir?.parentFile
        if (parentFile != null && parentFile.canonicalPath.contains(Environment.getExternalStorageDirectory().canonicalPath)) {
          viewModel.move(parentFile)
        } else {
          setResult(RESULT_CANCELED)
          finish()
        }
      }
      viewModel.init(intent.getStringArrayExtra(ALLOWED_FILE_EXTENSIONS_KEY), intent.hasExtra(DIRECTORY_ONLY_KEY), intent.hasExtra(ALLOW_CREATE_FILE_KEY))
      val context = LocalContext.current
      MarcelAndroidTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          Box(modifier = Modifier.fillMaxSize()) {
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
                onDismissRequest = {},
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
              ProposeFileDialog(viewModel, this@FilePickerActivity::finishWithResult)
              val showCreateFileDialog = remember { mutableStateOf(false) }
              CreateFileDialog(viewModel, showCreateFileDialog)
              if (viewModel.dirOnly || viewModel.allowCreateNewFile) {
                FloatingActionButton(
                  modifier = Modifier
                    .padding(all = 16.dp)
                    .size(64.dp)
                    .align(Alignment.BottomEnd),
                  onClick = {
                    if (viewModel.dirOnly) {
                      viewModel.proposedFile = viewModel.currentDir
                    } else {
                      showCreateFileDialog.value = true
                    }
                  }
                ) {
                  Icon(imageVector = if (viewModel.dirOnly) Icons.Filled.Done else Icons.Filled.Add,
                    contentDescription = if (viewModel.dirOnly) "Pick" else "Create new file")
                }
              }
            }
          }
        }
      }
    }
  }

  private fun finishWithResult(file: File) {
    setResult(RESULT_OK, Intent(intent).apply {
      putExtra(PICKED_FILE_PATH_KEY, file.absolutePath)
    })
    finish()
  }
}

@Composable
fun CreateFileDialog(viewModel: FileExplorerViewModel, show: MutableState<Boolean>) {
  if (!show.value) return
  val currentDir = viewModel.currentDir ?: return
  var fileName by remember { mutableStateOf("")  }
  var error by remember { mutableStateOf<String?>(null) }
  val context = LocalContext.current
  AlertDialog(
    onDismissRequest = { show.value = false },
    confirmButton = {
      TextButton(onClick = {
        if (fileName.isBlank()) {
          error = "Filename cannot be empty"
          return@TextButton
        }
        val file = File(currentDir, fileName)
        if (!file.createNewFile()) {
          error = "Invalid filename"
          return@TextButton
        }
        error = null
        Toast.makeText(context, "Created new file successfully", Toast.LENGTH_SHORT).show()
        viewModel.move(currentDir) // force reload
        show.value = false
      }) {
        Text(text = "Create")
      }
    },
    title = {
      Text(text = "Create new file")
    },
    text = {
      OutlinedTextField(
        value = fileName,
        onValueChange = { fileName = it},
        singleLine = true,
        label = { Text("Filename") },
        supportingText = error?.let { error -> {
          Text(
            modifier = Modifier.fillMaxWidth(),
            text = error,
            color = MaterialTheme.colorScheme.error
          )
        }},
        isError = error != null
      )
    },
    dismissButton = {
      TextButton(onClick = { show.value = false }) {
        Text(text = "Cancel")
      }
    },
  )
}

@Composable
fun ProposeFileDialog(viewModel: FileExplorerViewModel, onConfirm: (File) -> Unit) {
  val file = viewModel.proposedFile ?: return
  AlertDialog(
    onDismissRequest = { viewModel.proposedFile = null },
    title = {
      Text(text = "Pick ${file.name}?")
    },
    confirmButton = {
      TextButton(onClick = { onConfirm.invoke(file) }) {
        Text(text = "Yes")
      }
    },
    dismissButton = {
      TextButton(onClick = { viewModel.proposedFile = null }) {
        Text(text = "No")
      }
    },
  )
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
            .clickable {
              if (file.isDirectory) viewModel.move(file)
              else viewModel.proposedFile = file
            }
            .height(60.dp)
            .fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(modifier = Modifier.size(60.dp)) {
            Icon(
              modifier = Modifier
                .size(35.dp)
                .align(Alignment.Center),
              painter = painterResource(id = if (file.isDirectory) R.drawable.folder else R.drawable.document),
              contentDescription = "file",
              tint = Color.White,
            )
          }
          Text(text = file.name)
        }
      }
    }

  }
}

@Composable
fun TopBar(viewModel: FileExplorerViewModel, backPressedDispatcher: OnBackPressedDispatcher) {
  Box(modifier = Modifier
    .fillMaxWidth()
    .height(TopBarHeight)
    .padding(horizontal = 8.dp)
  ) {
    IconButton(
      modifier = Modifier.size(TopBarIconSize).align(Alignment.CenterStart),
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
      Text(
        text = viewModel.fileName(viewModel.currentDir!!),
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        modifier = Modifier.align(Alignment.Center)
      )
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
  var proposedFile by mutableStateOf<File?>(null)
  val filesPath = mutableStateListOf<File>()
  val children = mutableStateListOf<File>()
  var allowCreateNewFile by mutableStateOf(false)
  var dirOnly by mutableStateOf(false)

  var fileExtensions: Array<String>? = null

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
    currentDir.listFiles(this::filter)?.let { children ->
      // TODO make sorting criteria configurable
      this.children.addAll(children)
      this.children.sortBy { it.name }
    }
  }

  private fun filter(file: File): Boolean {
    return (fileExtensions == null || file.isDirectory || fileExtensions!!.any { file.name.endsWith(it) })
        && (!dirOnly || file.isDirectory)
  }

  fun fileName(f: File) = if (f == Environment.getExternalStorageDirectory()) "Internal Storage" else f.name

  fun init(fileExtensions: Array<String>?, dirOnly: Boolean, allowCreateNewFile: Boolean) {
    this.fileExtensions = fileExtensions
    this.dirOnly = dirOnly
    this.allowCreateNewFile = allowCreateNewFile
  }
}