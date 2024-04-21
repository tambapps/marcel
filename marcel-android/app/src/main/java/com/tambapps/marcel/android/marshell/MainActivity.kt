
package com.tambapps.marcel.android.marshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tambapps.marcel.android.marshell.repl.ShellSessionFactory
import com.tambapps.marcel.android.marshell.ui.component.IconButton
import com.tambapps.marcel.android.marshell.ui.component.ShellScreen
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.theme.MarcelAndroidTheme
import com.tambapps.marcel.android.marshell.ui.theme.TopBarIconSize
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var shellSessionFactory: ShellSessionFactory

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val navController = rememberNavController()
      val drawerState = rememberDrawerState(DrawerValue.Closed)
      MarcelAndroidTheme {
        NavigationDrawer(drawerState = drawerState) {
          Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
            NavHost(navController = navController, startDestination = "profile", modifier = Modifier.fillMaxSize()) {
              composable("profile") {
                ShellScreen(shellSessionFactory)
              }
              /*...*/
            }
            TopBar(drawerState) // putting it at the end because we want it to have top priority in terms of displaying
          }
        }
      }
    }
  }
}

@Composable
fun TopBar(drawerState: DrawerState) {
  val scope = rememberCoroutineScope()
  TopBarLayout {
    IconButton(
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
fun NavigationDrawer(drawerState: DrawerState, content: @Composable () -> Unit) {
  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet {
        Text("Marcel for Android", modifier = Modifier.padding(16.dp))
        HorizontalDivider()
        NavigationDrawerItem(
          label = { Text(text = "Drawer Item") },
          selected = false,
          onClick = { /*TODO*/ }
        )
        // ...other drawer items
      }
    }
    , content = content)
}
