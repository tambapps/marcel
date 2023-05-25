package com.tambapps.marcel.android.marshell

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ListUpdateCallback
import com.google.android.material.navigation.NavigationView
import com.tambapps.marcel.android.marshell.data.ShellSession
import com.tambapps.marcel.android.marshell.databinding.ActivityMainBinding
import com.tambapps.marcel.android.marshell.ui.shell.ShellFragment
import com.tambapps.marcel.android.marshell.util.ListenableList
import com.tambapps.marcel.android.marshell.util.hideSoftBoard
import dagger.hilt.android.AndroidEntryPoint
import marcel.lang.MarcelSystem
import java.io.File
import java.util.Collections
import javax.inject.Inject
import javax.inject.Named


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), DrawerLayout.DrawerListener,
  NavigationView.OnNavigationItemSelectedListener, ShellHandler {

  @Inject
  @Named("shellSessionsDirectory")
  lateinit var shellSessionsDirectory: File
  private lateinit var binding: ActivityMainBinding
  private lateinit var navController: NavController

  private lateinit var shellSessions: ListenableList<ShellSession>
  private var sessionsIncrement = 0
  override val sessionsCount get() = shellSessions.size
  override val sessions: List<ShellSession>
    get() = Collections.unmodifiableList(shellSessions)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    shellSessions = ListenableList()
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
    hideSoftBoard(binding.root)
  }

  override fun onDrawerClosed(drawerView: View) {
  }

  override fun onDrawerStateChanged(newState: Int) {
  }

  override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
    binding.drawerLayout.closeDrawer(binding.navView)
    if (NavigationUI.onNavDestinationSelected(menuItem, navController)) return true
    when (menuItem.itemId) {
      R.id.nav_playgrounds -> Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
      R.id.nav_documentation -> {
        CustomTabsIntent.Builder()
          .build()
          .launchUrl(this@MainActivity, Uri.parse("https://tambapps.github.io/marcel"))
      }
    }
    return false
  }

  override fun getSessionAt(i: Int): ShellSession {
    return shellSessions[i]
  }
  override fun startNewSession(): Boolean {
    if (shellSessions.size >= 8) {
      return false
    }
    val sessionDirectory = File(shellSessionsDirectory, "session_" + (sessionsIncrement++))
    // if directory already exists, we clean it
    if (sessionDirectory.exists()) {
      if (sessionDirectory.isFile) sessionDirectory.delete()
      else sessionDirectory.listFiles()?.forEach { it.deleteRecursively() }
    }
    if (!sessionDirectory.isDirectory && !sessionDirectory.mkdir()) {
      Toast.makeText(this, "Error, Couldn't create directory", Toast.LENGTH_SHORT).show()
      sessionsIncrement--
      return false
    }
    val name = if (sessionsIncrement == 1) "marshell" else "marshell ($sessionsIncrement)"
    shellSessions.add(ShellSession.newSession(name, sessionDirectory))
    return true
  }

  override fun stopSession(position: Int) {
    val shellSession = shellSessions.removeAt(position)
    shellSession.dispose()
    if (shellSessions.isEmpty()) {
      finish()
    }
  }
  override fun stopSession(shellSession: ShellSession): Boolean {
    val i = shellSessions.indexOf(shellSession)
    if (i < 0) return false
    stopSession(i)
    return true
  }

  override fun navigateToShell(scriptText: CharSequence, position: Int?) {
    if (shellSessions.isEmpty()) startNewSession()
    navController.navigate(R.id.nav_shell, Bundle().apply {
      putString(ShellFragment.SCRIPT_TEXT_ARG, scriptText.toString())
      if (position != null) {
        putInt(ShellFragment.SESSION_INDEX_ARG, position)
      }
    })
    binding.navView.setCheckedItem(R.id.nav_shell)
  }
  override fun registerCallback(callback: ListUpdateCallback) = shellSessions.registerCallback(callback)

  override fun unregisterCallback(callback: ListUpdateCallback) = shellSessions.unregisterCallback(callback)

  override fun onBackPressed() {
    super.onBackPressed()
    binding.navView.setCheckedItem(navController.currentDestination!!.id)
  }

  override fun onDestroy() {
    super.onDestroy()
    shellSessions.forEach { it.dispose() }
  }
}