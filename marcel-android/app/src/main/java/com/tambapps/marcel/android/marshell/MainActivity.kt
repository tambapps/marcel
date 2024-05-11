
package com.tambapps.marcel.android.marshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.service.PreferencesDataStore
import com.tambapps.marcel.android.marshell.service.ViewModelFactory
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellScreen
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.screen.editor.EditorScreen
import com.tambapps.marcel.android.marshell.ui.screen.editor.EditorViewModel
import com.tambapps.marcel.android.marshell.ui.screen.settings.SettingsScreen
import com.tambapps.marcel.android.marshell.ui.screen.settings.SettingsViewModel
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellViewModel
import com.tambapps.marcel.android.marshell.ui.screen.work.create.WorkCreateScreen
import com.tambapps.marcel.android.marshell.ui.screen.work.create.WorkCreateViewModel
import com.tambapps.marcel.android.marshell.ui.screen.work.list.WorksListScreen
import com.tambapps.marcel.android.marshell.ui.screen.work.list.WorksListViewModel
import com.tambapps.marcel.android.marshell.ui.screen.work.view.WorkViewModel
import com.tambapps.marcel.android.marshell.ui.screen.work.view.WorkViewScreen
import com.tambapps.marcel.android.marshell.ui.theme.MarcelAndroidTheme
import com.tambapps.marcel.android.marshell.ui.theme.TopBarIconSize
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import com.tambapps.marcel.android.marshell.work.ShellWorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Named

object Routes {
  const val SHELL = "shell"
  const val EDITOR = "editor"
  const val WORK_LIST = "work_list"
  const val WORK_CREATE = "work_create"
  const val WORK_VIEW = "work_view"
  const val SETTINGS = "settings"

  const val WORK_NAME_ARG = "workName"
  const val FILE_ARG = "file"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var shellWorkManager: ShellWorkManager
  @Inject
  lateinit var viewModelFactory: ViewModelFactory
  @Inject
  lateinit var preferencesDataStore: PreferencesDataStore
  @Inject
  @Named("shellSessionsDirectory")
  lateinit var shellSessionsDirectory: File
  @Inject
  lateinit var shellSessionFactory: ShellSessionFactory
  @Inject
  @Named("initScriptFile")
  lateinit var initScriptFile: File

  private lateinit var ioScope: CoroutineScope
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ioScope = CoroutineScope(Dispatchers.IO)
    setContent {
      val navController = rememberNavController()
      val drawerState = rememberDrawerState(DrawerValue.Closed)
      val scope = rememberCoroutineScope()
      MarcelAndroidTheme {
        NavigationDrawer(drawerState = drawerState, navController = navController, scope = scope) {
          Box(modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
            // instantiating shell VM here because we want to keep state even if we changed route and then go back to ShellScreen
            val shellViewModel: ShellViewModel = viewModelFactory.newInstance()
            NavHost(
              navController = navController,
              startDestination = Routes.SHELL,
              modifier = Modifier.fillMaxSize()
            ) {
              composable(Routes.SHELL) {
                ShellScreen(shellViewModel)
              }
              composable(
                Routes.EDITOR + "?${Routes.FILE_ARG}={${Routes.FILE_ARG}}",
                arguments = listOf(navArgument(Routes.WORK_NAME_ARG) { type = NavType.StringType; nullable = true })
              ) {
                val viewModel: EditorViewModel = viewModelFactory.newInstance()
                EditorScreen(viewModel)
              }
              composable(Routes.WORK_LIST) {
                val viewModel: WorksListViewModel = viewModelFactory.newInstance()
                WorksListScreen(viewModel = viewModel, navController = navController)
              }
              composable(Routes.WORK_CREATE) {
                val viewModel: WorkCreateViewModel = viewModelFactory.newInstance()
                WorkCreateScreen(viewModel, navController)
              }
              composable(Routes.WORK_VIEW + "/{${Routes.WORK_NAME_ARG}}", arguments = listOf(navArgument(Routes.WORK_NAME_ARG) { type = NavType.StringType })
              ) {
                val viewModel: WorkViewModel = viewModelFactory.newInstance()
                WorkViewScreen(viewModel, navController)
              }
              composable(Routes.SETTINGS) {
                val viewModel: SettingsViewModel = viewModelFactory.newInstance()
                SettingsScreen(viewModel, navController, initScriptFile)
              }
            }
            TopBar(drawerState, scope) // putting it at the end because we want it to have top priority in terms of displaying
          }
        }
      }
    }
  }

  override fun onDestroy() {
    shellSessionsDirectory.deleteRecursively()
    super.onDestroy()
  }

  override fun onResume() {
    super.onResume()
    ioScope.launch { shellWorkManager.runLateWorks() }
  }
}

@Composable
fun TopBar(drawerState: DrawerState, scope: CoroutineScope) {
  TopBarLayout {
    IconButton(
      modifier = Modifier.size(TopBarIconSize),
      onClick = {
        if (!drawerState.isAnimationRunning) {
          scope.launch {
            if (drawerState.isOpen) drawerState.close()
            else drawerState.open()
          }
        }
      },
    ) {
      Icon(
        modifier = Modifier.size(TopBarIconSize),
        imageVector = Icons.Filled.Menu,
        contentDescription = "Drawer",
        tint = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Composable
private fun NavigationDrawer(
  drawerState: DrawerState,
  navController: NavController,
  scope: CoroutineScope,
  content: @Composable () -> Unit
) {
  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.6f)
        ) {
        Box(modifier = Modifier.height(16.dp))
        Image(modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .size(64.dp), painter = painterResource(id = R.drawable.appicon), contentDescription = "marcel")
        Text("Marcel for Android", modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally), style = MaterialTheme.typography.shellTextStyle, fontWeight = FontWeight.Bold)
        HorizontalDivider()

        val backStackState = navController.currentBackStackEntryAsState()

        DrawerItem(
          navController = navController,
          drawerState = drawerState,
          scope = scope,
          text = "Shell",
          backStackState = backStackState,
          route = Routes.SHELL
        )

        DrawerItem(
          navController = navController,
          drawerState = drawerState,
          scope = scope,
          text = "Editor",
          selected = backStackState.value?.destination?.route?.startsWith("editor") ?: false,
          route = Routes.EDITOR
        )

        DrawerItem(
          navController = navController,
          drawerState = drawerState,
          scope = scope,
          text = "Workouts",
          selected = backStackState.value?.destination?.route?.startsWith("work") ?: false,
          route = Routes.WORK_LIST
        )

        DrawerItem(
          navController = navController,
          drawerState = drawerState,
          scope = scope,
          text = "Settings",
          backStackState = backStackState,
          route = Routes.SETTINGS
        )

      }
    }
    , content = content)
}

@Composable
private fun DrawerItem(
  navController: NavController,
  drawerState: DrawerState,
  backStackState: State<NavBackStackEntry?>,
  scope: CoroutineScope,
  text: String,
  route: String,
) = DrawerItem(navController, drawerState, backStackState.value?.destination?.route?.startsWith(route) ?: false, scope, text, route)

@Composable
private fun DrawerItem(
  navController: NavController,
  drawerState: DrawerState,
  selected: Boolean,
  scope: CoroutineScope,
  text: String,
  route: String,
) {
  NavigationDrawerItem(
    label = { Text(text = text, fontWeight = FontWeight.Bold) },
    selected = selected,
    shape = RectangleShape,
    onClick = {
      navController.navigate(route)
      scope.launch { drawerState.close() }
    }
  )
}