package com.tambapps.marcel.android.marshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tambapps.marcel.android.marshell.Routes.DOCUMENTATION
import com.tambapps.marcel.android.marshell.Routes.HOME
import com.tambapps.marcel.android.marshell.Routes.PATH_ARG
import com.tambapps.marcel.android.marshell.Routes.SESSION_ID
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.screen.documentation.DocumentationScreen
import com.tambapps.marcel.android.marshell.ui.screen.documentation.DocumentationViewModel
import com.tambapps.marcel.android.marshell.ui.theme.MarcelAndroidTheme
import com.tambapps.marcel.android.marshell.ui.theme.TopBarIconSize
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DocumentationActivity : ComponentActivity() {

  private val viewModel: DocumentationDrawerViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MarcelAndroidTheme {
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        NavigationDrawer(drawerState = drawerState, navController = navController, scope = scope, viewModel = viewModel) {
          Box(modifier = Modifier
            .background(MaterialTheme.colorScheme.background)) {
            NavHost(
              navController = navController,
              startDestination = DOCUMENTATION,
              modifier = Modifier.fillMaxSize()
            ) {
              composable("$DOCUMENTATION?$PATH_ARG={$PATH_ARG}",
                arguments = listOf(navArgument(PATH_ARG) { type = NavType.StringType; nullable = true })
              ) {
                DocumentationScreen()
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
private fun NavigationDrawer(
  drawerState: DrawerState,
  navController: NavController,
  scope: CoroutineScope,
  viewModel: DocumentationDrawerViewModel,
  content: @Composable () -> Unit
) {
  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.6f)
      ) {
        NavigationDrawerHeader()
        val backStackState = navController.currentBackStackEntryAsState()

        for (drawerEntry in viewModel.drawerEntries) {
          DocumentationDrawerItem(
            navController = navController,
            drawerState = drawerState,
            backStackState = backStackState,
            scope = scope,
            text = drawerEntry.text,
            path = drawerEntry.path
          )
        }
      }
    }, content = content)
}

@Composable
private fun DocumentationDrawerItem(
  navController: NavController,
  drawerState: DrawerState,
  backStackState: State<NavBackStackEntry?>,
  scope: CoroutineScope,
  text: String,
  path: String?,
) {
  DrawerItem(
    selected = backStackState.value?.destination?.route?.let { route ->
      if (path != null) route.contains("path=$path")
      else !route.contains("path")
    } == true,
    text = text,
    onClick = {
      navController.navigate(Routes.documentation(path))
      scope.launch { drawerState.close() }
    }
  )
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

@HiltViewModel
class DocumentationDrawerViewModel @Inject constructor(): ViewModel() {
  val drawerEntries = mutableStateListOf<DrawerEntry>(
    DrawerEntry("Home", null),
    DrawerEntry("Test", "coucou")
  )
}

data class DrawerEntry(val text: String, val path: String?)