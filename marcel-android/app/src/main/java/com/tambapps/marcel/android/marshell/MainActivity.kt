@file:OptIn(ExperimentalMaterial3Api::class)

package com.tambapps.marcel.android.marshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tambapps.marcel.android.marshell.ui.theme.MarcelAndroidTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val navController = rememberNavController()
      MarcelAndroidTheme {
        NavigationDrawer {
          NavHost(navController = navController, startDestination = "profile") {
            composable("profile") {
              Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                ShellScreen()
              }

            }
            /*...*/
          }
        }
      }
    }
  }
}

@Composable
fun NavigationDrawer(content: @Composable () -> Unit) {
  ModalNavigationDrawer(
    drawerContent = {
      ModalDrawerSheet {
        Text("Marcel for Android", modifier = Modifier.padding(16.dp))
        Divider()
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!",
    modifier = modifier
  )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MarcelAndroidTheme {
    Greeting("Android")
  }
}