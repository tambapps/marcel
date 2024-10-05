package com.tambapps.marcel.android.marshell

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.tambapps.marcel.android.marshell.Routes.CONSULT
import com.tambapps.marcel.android.marshell.Routes.EDITOR
import com.tambapps.marcel.android.marshell.Routes.EDIT_ARG
import com.tambapps.marcel.android.marshell.Routes.FILE_ARG
import com.tambapps.marcel.android.marshell.Routes.HOME
import com.tambapps.marcel.android.marshell.Routes.SESSION_ID
import com.tambapps.marcel.android.marshell.Routes.SETTINGS
import com.tambapps.marcel.android.marshell.Routes.SHELL
import com.tambapps.marcel.android.marshell.Routes.WORKOUT_FORM
import com.tambapps.marcel.android.marshell.Routes.WORKOUT_LIST
import com.tambapps.marcel.android.marshell.Routes.WORKOUT_NAME_ARG
import com.tambapps.marcel.android.marshell.Routes.WORKOUT_VIEW
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellScreen
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.screen.editor.EditorScreen
import com.tambapps.marcel.android.marshell.ui.screen.settings.SettingsScreen
import com.tambapps.marcel.android.marshell.ui.screen.shell.consult.ShellConsultScreen
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellViewModel
import com.tambapps.marcel.android.marshell.ui.screen.workout.create.WorkFormScreen
import com.tambapps.marcel.android.marshell.ui.screen.workout.list.WorksListScreen
import com.tambapps.marcel.android.marshell.ui.screen.workout.view.WorkViewScreen
import com.tambapps.marcel.android.marshell.ui.theme.MarcelAndroidTheme
import com.tambapps.marcel.android.marshell.ui.theme.TopBarIconSize
import com.tambapps.marcel.android.marshell.ui.theme.shellTextStyle
import com.tambapps.marcel.android.marshell.workout.ShellWorkoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var shellWorkoutManager: ShellWorkoutManager
  @Inject
  lateinit var shellSessionFactory: ShellSessionFactory

  private lateinit var ioScope: CoroutineScope
  private var sessionsIdIncrement = 1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ioScope = CoroutineScope(Dispatchers.IO)
    setContent {
      val navController = rememberNavController()
      val drawerState = rememberDrawerState(DrawerValue.Closed)
      val scope = rememberCoroutineScope()
      MarcelAndroidTheme {
        // instantiating shell VM here because we want to keep state even if we changed route and then go back to ShellScreen
        val defaultShellViewModel: ShellViewModel = hiltViewModel()
        val shellViewModels = remember {
          mutableStateMapOf(Pair(0, defaultShellViewModel))
        }
        NavigationDrawer(drawerState = drawerState, navController = navController, scope = scope, shellViewModels = shellViewModels, shellSessionFactory = shellSessionFactory) {
          Box(modifier = Modifier
            .background(MaterialTheme.colorScheme.background)) {
            val createNewShell: () -> Unit = remember { ({ createNewShell(scope, navController, drawerState, this@MainActivity, shellViewModels)}) }
            NavHost(
              navController = navController,
              startDestination = HOME,
              modifier = Modifier.fillMaxSize()
            ) {
              // need a startDestination without any parameters for deep links to work
              composable(HOME, scope, navController, drawerState) {
                val sessionId = shellViewModels.keys.min()
                ShellScreen(navController, shellViewModels.getValue(sessionId), sessionId, shellViewModels.size, createNewShell)
              }
              composable(
                "$SHELL/{$SESSION_ID}", scope, navController, drawerState,
                arguments = listOf(navArgument(SESSION_ID) { type = NavType.IntType }),
                enterTransition = {
                  return@composable expandIn(tween(700))
                }
              ) {
                val sessionId = it.arguments?.getInt(SESSION_ID, 0)
                val viewModel = shellViewModels[sessionId]
                if (sessionId != null && viewModel != null) {
                  ShellScreen(navController, viewModel, sessionId, shellViewModels.size, createNewShell)
                } else {
                  LaunchedEffect(Unit) {
                    navController.navigate(HOME)
                  }
                }
              }
              composable(CONSULT, scope, navController, drawerState,
                arguments = listOf(navArgument(SESSION_ID) { type = NavType.IntType })) {
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
                "$EDITOR?$FILE_ARG={$FILE_ARG}", scope, navController, drawerState,
                arguments = listOf(navArgument(FILE_ARG) { type = NavType.StringType; nullable = true })
              ) {
                EditorScreen()
              }
              composable(WORKOUT_LIST, scope, navController, drawerState) {
                WorksListScreen(navController = navController)
              }
              composable("$WORKOUT_FORM?$WORKOUT_NAME_ARG={$WORKOUT_NAME_ARG}&${EDIT_ARG}={${EDIT_ARG}}", scope, navController, drawerState,
                arguments = listOf(
                  navArgument(WORKOUT_NAME_ARG) { type = NavType.StringType; nullable = true },
                  navArgument(EDIT_ARG) { type = NavType.BoolType; defaultValue = false }
                ),
              ) {
                WorkFormScreen(navController)
              }
              composable(
                "$WORKOUT_VIEW/{$WORKOUT_NAME_ARG}", scope, navController, drawerState,
                arguments = listOf(navArgument(WORKOUT_NAME_ARG) { type = NavType.StringType }),
                deepLinks = listOf(navDeepLink {
                  uriPattern = "app://marshell/$WORKOUT_VIEW/{$WORKOUT_NAME_ARG}"
                })
              ) {
                WorkViewScreen(navController)
              }
              composable(SETTINGS, scope, navController, drawerState) {
                SettingsScreen(navController)
              }
            }
            TopBar(drawerState, scope) // putting it at the end because we want it to have top priority in terms of displaying
          }
        }
      }
    }
  }

  private fun createNewShell(scope: CoroutineScope, navController: NavController, drawerState: DrawerState, context: Context, shellViewModels: MutableMap<Int, ShellViewModel>) {
    scope.launch {
      drawerState.close()
      withContext(Dispatchers.IO) {
        val shellViewModel = ShellViewModel(context, shellSessionFactory)
        val id = sessionsIdIncrement++
        shellViewModels[id] = shellViewModel
        withContext(Dispatchers.Main) {
          navController.navigate("$SHELL/$id")
          Toast.makeText(
            context,
            "New shell $id has been started",
            Toast.LENGTH_SHORT
          ).show()
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
    ioScope.launch { shellWorkoutManager.runLateWorkouts() }
  }

  @Composable
  private fun NavigationDrawer(
    drawerState: DrawerState,
    navController: NavController,
    scope: CoroutineScope,
    shellViewModels: MutableMap<Int, ShellViewModel>,
    shellSessionFactory: ShellSessionFactory,
    content: @Composable () -> Unit
  ) {
    val context = LocalContext.current
    val showConfirmRemoveSessionDialog = remember { mutableStateOf<ShellViewModel?>(null) }
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
            for ((id, shellViewModel) in shellViewModels) {
              val route = "$SHELL/$id"
              Box {
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
                IconButton(onClick = {
                  showConfirmRemoveSessionDialog.value = shellViewModel
                  scope.launch { drawerState.close() }
                }, modifier = Modifier.align(Alignment.CenterEnd)) {
                  Icon(
                    Icons.Filled.Clear,
                    tint = Color.Red,
                    modifier = Modifier.size(23.dp),
                    contentDescription = "Remove session",
                  )
                }
              }
            }
          }
          if (shellViewModels.size > 1) HorizontalDivider(Modifier.padding(vertical = 2.dp))

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
            selected = backStackState.value?.destination?.route?.startsWith("workout") ?: false,
            route = WORKOUT_LIST
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
    val toDeleteShellViewModel = showConfirmRemoveSessionDialog.value ?: return
    val toDeleteShellSessionId = shellViewModels.entries.find { it.value == toDeleteShellViewModel }!!.key
    ConfirmDeleteSessionDialog(context, navController, showConfirmRemoveSessionDialog, shellViewModels, toDeleteShellSessionId)
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

@Composable
fun ConfirmDeleteSessionDialog(context: Context, navController: NavController, show: MutableState<ShellViewModel?>, shellViewModels: MutableMap<Int, ShellViewModel>, sessionId: Int) {
  AlertDialog(
    title = {
      Text(text = "Close Shell $sessionId?")
    },
    text = {
      Text(text = "This action is not reversible")
    },
    onDismissRequest = { show.value = null },
    dismissButton = {
      TextButton(onClick = { show.value = null }) {
        Text(text = "Cancel")
      }
    },
    confirmButton = {
      TextButton(onClick = {
        show.value = null
        if (shellViewModels.containsKey(sessionId) && shellViewModels.size > 1) {
          shellViewModels.remove(sessionId)
        }
        navController.navigate(HOME)
        Toast.makeText(context, "Shell $sessionId has been closed", Toast.LENGTH_SHORT).show()
      }) {
        Text(text = "Confirm", color = Color.Red)
      }
    }
  )
}

// util function that overrides the default navigation back press listener the way I want it
fun NavGraphBuilder.composable(
  route: String,
  scope: CoroutineScope,
  navController: NavController,
  drawerState: DrawerState,
  arguments: List<NamedNavArgument> = emptyList(),
  deepLinks: List<NavDeepLink> = emptyList(),
  enterTransition: (@JvmSuppressWildcards
  AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
  exitTransition: (@JvmSuppressWildcards
  AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
  popEnterTransition: (@JvmSuppressWildcards
  AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
    enterTransition,
  popExitTransition: (@JvmSuppressWildcards
  AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
    exitTransition,
  backPressedConfirmFinish: Boolean = true,
  content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit

) {
  composable(route, arguments, deepLinks, enterTransition, exitTransition, popEnterTransition, popExitTransition) {
    content.invoke(this, it)
    BackPressHandler(scope, navController, drawerState, backPressedConfirmFinish)
  }
}

@Composable
private fun BackPressHandler(scope: CoroutineScope, navController: NavController, drawerState: DrawerState, confirmFinish: Boolean) {
  val context = LocalContext.current
  var lastBackPressedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis())  }

  // the override of the default navigation back press listener
  BackHandler {
    if (drawerState.isOpen) {
      scope.launch { drawerState.close() }
    } else if (!navController.popBackStack()) {
      if (confirmFinish) {
        if (System.currentTimeMillis() - lastBackPressedTimestamp < 750) {
          (context as? Activity)?.finish()
        } else {
          lastBackPressedTimestamp = System.currentTimeMillis()
          Toast.makeText(context, "Press back again to close the app", Toast.LENGTH_SHORT)
            .show()
        }
      } else {
        (context as? Activity)?.finish()
      }
    }
  }
}