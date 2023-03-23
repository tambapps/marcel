package com.tambapps.marcel.android.app

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.tambapps.marcel.android.app.databinding.ActivityMainBinding
import com.tambapps.marcel.android.app.util.ContextUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), DrawerLayout.DrawerListener,
  NavigationView.OnNavigationItemSelectedListener {

  private lateinit var binding: ActivityMainBinding
  private lateinit var navController: NavController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)


    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val drawer: DrawerLayout = binding.drawerLayout
    val navigatorView: NavigationView = binding.navView

    findViewById<View>(R.id.drawerButton).setOnClickListener {
      if (drawer.isDrawerOpen(navigatorView)) {
        drawer.closeDrawer(navigatorView)
      } else {
        drawer.openDrawer(navigatorView)
        val view = currentFocus
        if (view != null) {
          ContextUtils.hideSoftBoard(this, view)
        }
      }
    }
    drawer.addDrawerListener(this)
    navController = Navigation.findNavController(this, R.id.nav_host_fragment)
    navigatorView.setNavigationItemSelectedListener(this)
    navigatorView.setCheckedItem(R.id.nav_shell)
  }

  override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
  }

  override fun onDrawerOpened(drawerView: View) {
  }

  override fun onDrawerClosed(drawerView: View) {
    ContextUtils.hideSoftBoard(this, drawerView)
  }

  override fun onDrawerStateChanged(newState: Int) {
  }

  override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
    if (!NavigationUI.onNavDestinationSelected(menuItem, navController)) {
      // note to self we're only going here if there is no entry for the nav_something in the mobile_navigation.xml
      when (menuItem.itemId) {
        // to do
      }
    }

    binding.drawerLayout.closeDrawer(binding.navView)
    return true

  }

}