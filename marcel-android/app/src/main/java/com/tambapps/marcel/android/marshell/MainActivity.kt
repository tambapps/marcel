package com.tambapps.marcel.android.marshell

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.tambapps.marcel.android.marshell.Routes.CONSULT
import com.tambapps.marcel.android.marshell.Routes.DELETE_SHELL
import com.tambapps.marcel.android.marshell.Routes.EDITOR
import com.tambapps.marcel.android.marshell.Routes.FILE_ARG
import com.tambapps.marcel.android.marshell.Routes.HOME
import com.tambapps.marcel.android.marshell.Routes.NEW_SHELL
import com.tambapps.marcel.android.marshell.Routes.SESSION_ID
import com.tambapps.marcel.android.marshell.Routes.SETTINGS
import com.tambapps.marcel.android.marshell.Routes.SHELL
import com.tambapps.marcel.android.marshell.Routes.WORK_CREATE
import com.tambapps.marcel.android.marshell.Routes.WORK_LIST
import com.tambapps.marcel.android.marshell.Routes.WORK_NAME_ARG
import com.tambapps.marcel.android.marshell.Routes.WORK_VIEW
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellScreen
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.screen.editor.EditorScreen
import com.tambapps.marcel.android.marshell.ui.screen.settings.SettingsScreen
import com.tambapps.marcel.android.marshell.ui.screen.shell.consult.ShellConsultScreen
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellViewModel
import com.tambapps.marcel.android.marshell.ui.screen.work.create.WorkCreateScreen
import com.tambapps.marcel.android.marshell.ui.screen.work.list.WorksListScreen
import com.tambapps.marcel.android.marshell.ui.screen.work.view.WorkViewScreen
import com.tambapps.marcel.android.marshell.ui.theme.MarcelAndroidTheme
import com.tambapps.marcel.android.marshell.ui.theme.TopBarIconSize
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import com.tambapps.marcel.android.marshell.work.ShellWorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var shellWorkManager: ShellWorkManager
  @Inject
  lateinit var shellSessionFactory: ShellSessionFactory

  private lateinit var ioScope: CoroutineScope
  private var sessionsIdIncrement = 1
  private var closeTimestamp = System.currentTimeMillis()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ioScope = CoroutineScope(Dispatchers.IO)
    setContent {
      val navController = rememberNavController()
      val drawerState = rememberDrawerState(DrawerValue.Closed)
      val scope = rememberCoroutineScope()
      onBackPressedDispatcher.addCallback {
        if (drawerState.isOpen) {
          scope.launch { drawerState.close() }
        } else if (System.currentTimeMillis() - closeTimestamp >= 750) {
          closeTimestamp = System.currentTimeMillis()
          Toast.makeText(this@MainActivity, "Press back again to close the app", Toast.LENGTH_SHORT)
            .show()
        } else {
          finish()
        }
      }
      MarcelAndroidTheme {
        // instantiating shell VM here because we want to keep state even if we changed route and then go back to ShellScreen
        val defaultShellViewModel: ShellViewModel = hiltViewModel()
        val shellViewModels = remember {
          mutableStateMapOf(Pair(0, defaultShellViewModel))
        }
        NavigationDrawer(drawerState = drawerState, navController = navController, scope = scope, shellViewModels = shellViewModels) {
          Box(modifier = Modifier
            .background(MaterialTheme.colorScheme.background)) {
            NavHost(
              navController = navController,
              startDestination = HOME,
              modifier = Modifier.fillMaxSize()
            ) {
              // need a startDestination without any parameters for deep links to work
              composable(HOME) {
                val sessionId = shellViewModels.keys.min()
                ShellScreen(navController, shellViewModels.getValue(sessionId), sessionId, shellViewModels.size)
              }
              composable(NEW_SHELL) {
                val viewModel: ShellViewModel = hiltViewModel()

                LaunchedEffect(Unit) {
                  val id = sessionsIdIncrement++
                  shellViewModels[id] = viewModel
                  navController.navigate("$SHELL/$id") {
                    popUpTo(NEW_SHELL) { inclusive = true }
                  }
                }
              }
              composable(
                DELETE_SHELL,
                arguments = listOf(navArgument(SESSION_ID) { type = NavType.IntType })
              ) {
                val sessionId = it.arguments?.getInt(SESSION_ID, Int.MAX_VALUE)
                LaunchedEffect(Unit) {
                  if (sessionId != null && shellViewModels.containsKey(sessionId) && shellViewModels.size > 1) {
                    shellViewModels.remove(sessionId)
                  }
                  navController.navigate(HOME)
                }
              }

              composable(
                "$SHELL/{$SESSION_ID}",
                arguments = listOf(navArgument(SESSION_ID) { type = NavType.IntType })
              ) {
                val sessionId = it.arguments?.getInt(SESSION_ID, 0)
                val viewModel = shellViewModels[sessionId]
                if (sessionId != null && viewModel != null) {
                  ShellScreen(navController, viewModel, sessionId, shellViewModels.size)
                } else {
                  LaunchedEffect(Unit) {
                    navController.navigate(HOME)
                  }
                }
              }
              composable(CONSULT, arguments = listOf(navArgument(SESSION_ID) { type = NavType.IntType })) {
                val sessionId = it.arguments?.getInt(SESSION_ID, 0)
                val viewModel = shellViewModels[sessionId]
                if (sessionId != null && viewModel != null) {
                  ShellConsultScreen(viewModel)
                } else {
                  LaunchedEffect(Unit) {
                    navController.navigate(HOME)
                  }
                }
              }
              composable(
                "$EDITOR?$FILE_ARG={$FILE_ARG}",
                arguments = listOf(navArgument(WORK_NAME_ARG) { type = NavType.StringType; nullable = true })
              ) {
                EditorScreen()
              }
              composable(WORK_LIST) {
                WorksListScreen(navController = navController)
              }
              composable(WORK_CREATE) {
                WorkCreateScreen(navController)
              }
              composable(
                route = "$WORK_VIEW/{$WORK_NAME_ARG}",
                arguments = listOf(navArgument(WORK_NAME_ARG) { type = NavType.StringType }),
                deepLinks = listOf(navDeepLink {
                  uriPattern = "app://marshell/$WORK_VIEW/{$WORK_NAME_ARG}"
                })
              ) {
                WorkViewScreen(navController)
              }
              composable(SETTINGS) {
                SettingsScreen(navController)
              }
            }
            TopBar(drawerState, scope) // putting it at the end because we want it to have top priority in terms of displaying
          }
        }
      }
    }
  }

  override fun onDestroy() {
    shellSessionFactory.dispose()
    super.onDestroy()
  }

  override fun onResume() {
    super.onResume()
    ioScope.launch { shellWorkManager.runLateWorks() }
  }
}

@Composable
private fun TopBar(drawerState: DrawerState, scope: CoroutineScope) {
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
fun ColumnScope.NavigationDrawerHeader() {
  Box(modifier = Modifier.height(16.dp))
  Image(modifier = Modifier
    .align(Alignment.CenterHorizontally)
    .size(64.dp), painter = painterResource(id = R.drawable.appicon), contentDescription = "marcel")
  Text("Marcel for Android", modifier = Modifier
    .padding(16.dp)
    .align(Alignment.CenterHorizontally), style = MaterialTheme.typography.shellTextStyle, fontWeight = FontWeight.Bold)
  HorizontalDivider(Modifier.padding(vertical = 2.dp))
}

@Composable
private fun NavigationDrawer(
  drawerState: DrawerState,
  navController: NavController,
  scope: CoroutineScope,
  shellViewModels: Map<Int, ShellViewModel>,
  content: @Composable () -> Unit
) {
  val context = LocalContext.current
  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.6f)
        ) {
        NavigationDrawerHeader()
        val backStackState = navController.currentBackStackEntryAsState()

        val defaultShellSessionId = shellViewModels.keys.min()
        if (shellViewModels.size == 1) {
          DrawerItem(
            navController = navController,
            drawerState = drawerState,
            scope = scope,
            text = "Shell",
            selected = backStackState.value?.let { it.destination.route?.startsWith(HOME) == true || it.arguments?.getInt(SESSION_ID, Int.MAX_VALUE) == defaultShellSessionId } == true,
            route = HOME
          )
        } else {
          for (id in shellViewModels.keys) {
            val route = "$SHELL/$id"
            DrawerItem(
              navController = navController,
              drawerState = drawerState,
              scope = scope,
              text = "Shell $id",
              selected = backStackState.value?.let {
                id == defaultShellSessionId && it.destination.route == HOME || it.arguments?.getInt(SESSION_ID) == id
              } ?: false,
              route = route
            )
          }
        }
        DrawerItem(
          navController = navController,
          drawerState = drawerState,
          scope = scope,
          text = "New shell",
          backStackState = backStackState,
          route = NEW_SHELL
        )
        HorizontalDivider(Modifier.padding(vertical = 2.dp))

        DrawerItem(
          navController = navController,
          drawerState = drawerState,
          scope = scope,
          text = "Editor",
          backStackState = backStackState,
          route = EDITOR
        )

        DrawerItem(
          navController = navController,
          drawerState = drawerState,
          scope = scope,
          text = "Shell Workouts",
          selected = backStackState.value?.destination?.route?.startsWith("work") ?: false,
          route = WORK_LIST
        )

        DrawerItem(
          navController = navController,
          drawerState = drawerState,
          scope = scope,
          text = "Settings",
          backStackState = backStackState,
          route = SETTINGS
        )
        DrawerItem(
          selected = false,
          text = "Documentation",
          onClick = {
            context.startActivity(Intent(context, DocumentationActivity::class.java))
            scope.launch { drawerState.close() }
          }
        )

      }
    }
    , content = content)
}

@Composable
fun DrawerItem(
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
  DrawerItem(
    selected = selected,
    text = text,
    onClick = {
      navController.navigate(route)
      scope.launch { drawerState.close() }
    }
  )
}

@Composable
fun DrawerItem(
  selected: Boolean,
  text: String,
  onClick: () -> Unit,
) {
  NavigationDrawerItem(
    label = { Text(text = text, fontWeight = FontWeight.Bold) },
    selected = selected,
    shape = RectangleShape,
    onClick = onClick
  )
}