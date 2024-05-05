
package com.tambapps.marcel.android.marshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tambapps.marcel.android.marshell.service.PreferencesDataStore
import com.tambapps.marcel.android.marshell.service.ViewModelFactory
import com.tambapps.marcel.android.marshell.ui.component.IconButton
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellScreen
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.screen.editor.EditorScreen
import com.tambapps.marcel.android.marshell.ui.screen.editor.EditorViewModel
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellViewModel
import com.tambapps.marcel.android.marshell.ui.screen.work.create.WorkCreateScreen
import com.tambapps.marcel.android.marshell.ui.screen.work.create.WorkCreateViewModel
import com.tambapps.marcel.android.marshell.ui.screen.work.list.WorksListScreen
import com.tambapps.marcel.android.marshell.ui.screen.work.list.WorksListViewModel
import com.tambapps.marcel.android.marshell.ui.screen.work.view.WorkViewModel
import com.tambapps.marcel.android.marshell.ui.screen.work.view.WorkViewScreen
import com.tambapps.marcel.android.marshell.ui.theme.MarcelAndroidTheme
import com.tambapps.marcel.android.marshell.ui.theme.TopBarIconSize
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

object Routes {
  const val SHELL = "shell"
  const val EDITOR = "editor"
  const val WORK_LIST = "work_list"
  const val WORK_CREATE = "work_create"
  const val WORK_VIEW = "work_view"
  const val SETTINGS = "settings"

  const val WORK_NAME_ARG = "workName"
}
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var viewModelFactory: ViewModelFactory
  @Inject
  lateinit var preferencesDataStore: PreferencesDataStore

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
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
              composable(Routes.EDITOR) {
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
              ) { backStackEntry ->
                val viewModel: WorkViewModel = viewModelFactory.newInstance()
                val workName = backStackEntry.arguments?.getString(Routes.WORK_NAME_ARG)
                if (workName != null) {
                  viewModel.init(workName)
                }
                WorkViewScreen(viewModel)
              }
              composable(Routes.SETTINGS) {
                // TODO
              }

            }
            TopBar(drawerState, scope) // putting it at the end because we want it to have top priority in terms of displaying
          }
        }
      }
    }
  }
}

@Composable
fun TopBar(drawerState: DrawerState, scope: CoroutineScope) {
  TopBarLayout {
    IconButton(
      modifier = Modifier.size(TopBarIconSize),
      imageVector = Icons.Filled.Menu,
      size = TopBarIconSize,
      onClick = {
        if (!drawerState.isAnimationRunning) {
          scope.launch {
            if (drawerState.isOpen) drawerState.close()
            else drawerState.open()
          }
        }
      }
    )
  }
}

@Composable
fun NavigationDrawer(
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
        Text("Marcel for Android", modifier = Modifier.padding(16.dp))
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
          backStackState = backStackState,
          route = Routes.EDITOR
        )

        DrawerItem(
          navController = navController,
          drawerState = drawerState,
          scope = scope,
          text = "Workouts",
          selected = backStackState.value?.destination?.route?.let { it == Routes.WORK_LIST || it == Routes.WORK_CREATE || it.startsWith(Routes.WORK_VIEW) } ?: false,
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
) = DrawerItem(navController, drawerState, backStackState.value?.destination?.route == route, scope, text, route)

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
    label = { Text(text = text) },
    selected = selected,
    onClick = {
      navController.navigate(route)
      scope.launch { drawerState.close() }
    }
  )
}