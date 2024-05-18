package com.tambapps.marcel.android.marshell.ui.screen.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tambapps.marcel.android.marshell.ui.theme.TopBarHeight


object ConsultRoutes {
  const val VARIABLES = "variables"
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
    NavigationGraph(navController = navController, modifier = Modifier.weight(1f))

  }
}


@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier) {
  NavHost(modifier = modifier, navController = navController, startDestination = ConsultRoutes.VARIABLES) {
    composable(ConsultRoutes.VARIABLES) {
      Text(text = ConsultRoutes.VARIABLES)
    }
    composable(ConsultRoutes.IMPORTS) {
      Text(text = ConsultRoutes.IMPORTS)
    }
    composable(ConsultRoutes.DUMBBELLS) {
      Text(text = ConsultRoutes.DUMBBELLS)
    }
  }
}