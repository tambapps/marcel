
package com.tambapps.marcel.android.marshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tambapps.marcel.android.marshell.ui.theme.MarcelAndroidTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val navController = rememberNavController()
      val drawerState = rememberDrawerState(DrawerValue.Closed)
      val scope = rememberCoroutineScope()
      MarcelAndroidTheme {
        Scaffold(topBar = {
          Row {
            IconButton(
              onClick = {
                if (!drawerState.isAnimationRunning) {
                  scope.launch {
                    if (drawerState.isOpen) drawerState.close()
                    else drawerState.open()
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                tint = Color.White
              )
            }
          }
        },
          content = { contentPadding ->
            NavigationDrawer(drawerState = drawerState) {
              Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(contentPadding)) {
                NavHost(navController = navController, startDestination = "profile", modifier = Modifier.fillMaxSize()) {
                  composable("profile") {
                    Surface(modifier = Modifier.fillMaxSize()) {
                      ShellScreen()
                    }

                  }
                  /*...*/
                }
              }
            }

          })
      }
    }
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
