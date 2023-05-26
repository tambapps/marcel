package com.tambapps.marcel.android.marshell

import android.app.NotificationManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.tambapps.marcel.android.marshell.ui.shellwork.view.ShellWorkViewFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShellWorkViewActivity : AppCompatActivity() {

  companion object {
    const val NOTIFICATION_ID_KEY = "notificationId"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_fragment)
    if (savedInstanceState == null) {

      val workName = intent.getStringExtra(ShellWorkViewFragment.SHELL_WORK_NAME_KEY)
      println("cacacaca ${intent.extras}")
      if (workName == null) {
        Toast.makeText(applicationContext, "Work couldn't be found", Toast.LENGTH_SHORT).show()
        finish()
        return
      }
      val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.cancel(intent.getIntExtra(NOTIFICATION_ID_KEY, 0))

      supportFragmentManager.beginTransaction()
        .replace(R.id.container, ShellWorkViewFragment.newInstance(workName = workName))
        .commitNow()
    }
  }
}