package com.tambapps.marcel.android.marshell.ui.screen.shell.consult

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.ui.screen.shell.ShellViewModel
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight


object ConsultRoutes {
  const val VARIABLES = "variables"
  const val FUNCTIONS = "functions"
  const val CLASSES = "classes"
  const val IMPORTS = "imports"
  const val DUMBBELLS = "dumbbells"
}
@Composable
fun ShellConsultScreen(
  viewModel: ShellViewModel
) {
  val navController = rememberNavController()

  Column(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.padding(TopBarHeight))
    NavigationGraph(navController = navController, viewModel = viewModel)
    BottomNavigationBar(navController)
  }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
  val backStackState = navController.currentBackStackEntryAsState()

  NavigationBar {
    BottomNavigationBarItem(
      icon = R.drawable.variable,
      route = ConsultRoutes.VARIABLES,
      text = "Variables",
      navController = navController,
      backStackState = backStackState
    )
    BottomNavigationBarItem(
      icon = R.drawable.function,
      route = ConsultRoutes.FUNCTIONS,
      text = "Functions",
      navController = navController,
      backStackState = backStackState
    )
    BottomNavigationBarItem(
      icon = R.drawable.brackets,
      route = ConsultRoutes.CLASSES,
      text = "Classes",
      navController = navController,
      backStackState = backStackState
    )
    BottomNavigationBarItem(
      icon = R.drawable.package_,
      route = ConsultRoutes.IMPORTS,
      text = "Imports",
      navController = navController,
      backStackState = backStackState
    )

    BottomNavigationBarItem(
      icon = R.drawable.dumbell,
      route = ConsultRoutes.DUMBBELLS,
      text = "Dumbbells",
      navController = navController,
      backStackState = backStackState
    )
  }
}

@Composable
private fun RowScope.BottomNavigationBarItem(
  icon: Int,
  route: String,
  text: String,
  navController: NavHostController,
  backStackState: State<NavBackStackEntry?>
) {
  NavigationBarItem(
    selected = backStackState.value?.destination?.route?.startsWith(route) == true,
    onClick = {
      val currentRoute = backStackState.value?.destination?.route
      navController.navigate(route) {
        if (currentRoute != null) {
          popUpTo(currentRoute) { inclusive = true }
        }
      }
    },
    icon = {
      Icon(
        modifier = Modifier.size(32.dp),
        painter = painterResource(id = icon),
        contentDescription = text
      )
    },
    label = {
      Text(text = text)
    }
  )
}
@Composable
private fun ColumnScope.NavigationGraph(
  navController: NavHostController,
  viewModel: ShellViewModel,
) {
  NavHost(modifier = Modifier
    .weight(1f)
    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
    navController = navController, startDestination = ConsultRoutes.VARIABLES) {
    composable(ConsultRoutes.VARIABLES) {
      VariablesScreen(viewModel)
    }
    composable(ConsultRoutes.FUNCTIONS) {
      FunctionsScreen(viewModel)
    }
    composable(ConsultRoutes.CLASSES) {
      ClassesScreen(viewModel)
    }
    composable(ConsultRoutes.IMPORTS) {
      ImportsScreen(viewModel)
    }
    composable(ConsultRoutes.DUMBBELLS) {
      Text(text = ConsultRoutes.DUMBBELLS)
    }
  }
}