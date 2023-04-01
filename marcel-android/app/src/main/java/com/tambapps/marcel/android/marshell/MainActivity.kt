package com.tambapps.marcel.android.marshell

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.tambapps.marcel.android.marshell.data.ShellSession
import com.tambapps.marcel.android.marshell.databinding.ActivityMainBinding
import com.tambapps.marcel.android.marshell.util.hideSoftBoard
import dagger.hilt.android.AndroidEntryPoint
import marcel.lang.MarcelSystem

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), DrawerLayout.DrawerListener,
  NavigationView.OnNavigationItemSelectedListener, ShellHandler {

  private lateinit var binding: ActivityMainBinding
  private lateinit var navController: NavController

  private lateinit var _shellSessions: MutableList<ShellSession>
  override val shellSessions: List<ShellSession> get() = _shellSessions

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    _shellSessions = mutableListOf()
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
          hideSoftBoard(view)
        }
      }
    }
    drawer.addDrawerListener(this)
    navController = Navigation.findNavController(this, R.id.nav_host_fragment)
    navigatorView.setNavigationItemSelectedListener(this)
    navigatorView.setCheckedItem(R.id.nav_shell)

    startNewSession()
  }

  override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
  }

  override fun onDrawerOpened(drawerView: View) {
  }

  override fun onDrawerClosed(drawerView: View) {
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

  override fun startNewSession(): Boolean {
    if (_shellSessions.size >= 8) {
      return false
    }
    _shellSessions.add(ShellSession(this))
    return true
  }

  override fun onStop() {
    super.onStop()
    MarcelSystem.setPrinter(null)
  }

  override fun onDestroy() {
    super.onDestroy()
    shellSessions.forEach { it.dispose() }
  }
}