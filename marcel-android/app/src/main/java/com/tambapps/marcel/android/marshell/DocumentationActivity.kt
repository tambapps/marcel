package com.tambapps.marcel.android.marshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.tambapps.marcel.android.marshell.Routes.PATH_ARG
import com.tambapps.marcel.android.marshell.service.DocumentationMdStore
import com.tambapps.marcel.android.marshell.ui.component.TopBarLayout
import com.tambapps.marcel.android.marshell.ui.screen.documentation.DocumentationScreen
import com.tambapps.marcel.android.marshell.ui.theme.MarcelAndroidTheme
import com.tambapps.marcel.android.marshell.ui.theme.TopBarIconSize
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.BulletList
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.node.Text
import javax.inject.Inject

@AndroidEntryPoint
class DocumentationActivity : ComponentActivity() {

  private val viewModel: DocumentationDrawerViewModel by viewModels()
  @Inject
  lateinit var documentationStore: DocumentationMdStore
  private val ioScope = CoroutineScope(Dispatchers.IO)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ioScope.launch {
      documentationStore.get(
        DocumentationMdStore.SUMMARY,
        onSuccess = {
          val visitor = SummaryMdVisitor()
          it.accept(visitor)
          viewModel.drawerEntries.clear()
          viewModel.drawerEntries.addAll(visitor.collectedEntries)
        },
        onError = {}
      )
    }
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

// TODO do custom welcome page for marcel for android and skip the whole getting started block here
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
      ModalDrawerSheet(modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth(0.85f)) {
        NavigationDrawerHeader()
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
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
  val routePath = backStackState.value?.arguments?.getString("path")
  DrawerItem(
    selected = when {
      routePath == path -> true
      routePath != null && path != null -> routePath.contains(path)
      else -> false
    },
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
  val drawerEntries = mutableStateListOf<DrawerEntry>()
}

data class DrawerEntry(val depth: Int, val text: String, val path: String?)

class SummaryMdVisitor: AbstractVisitor() {

  private var depth = 0
  private val steps = mutableListOf<Int>() // keeping track of steps, like 1.2.1
  private val drawerEntries = mutableListOf<DrawerEntry>()
  val collectedEntries: List<DrawerEntry> get() = drawerEntries

  override fun visit(bulletList: BulletList) {
    depth++
    steps.add(0)
    super.visit(bulletList)
    depth--
    steps.removeLast()
  }

  override fun visit(listItem: ListItem) {
    val linkNode = (listItem.firstChild as? Paragraph)?.firstChild as? Link
    val title = (linkNode?.firstChild as? Text)?.literal
    if (linkNode != null && title != null) {
      steps[steps.lastIndex] = steps.last() + 1
      val stepText = (steps).joinToString(separator = ".", postfix = " ")
      drawerEntries.add(DrawerEntry(depth,
        "\t\t".repeat(steps.size - 1) + stepText + title,
        linkNode.destination))
    }
    super.visit(listItem)
  }
}