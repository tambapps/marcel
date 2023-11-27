package com.tambapps.marcel.android.marshell

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.tambapps.marcel.android.marshell.ui.shellwork.view.ShellWorkScriptEditorFragment
import com.tambapps.marcel.android.marshell.ui.shellwork.view.ShellWorkScriptEditorFragment.Companion.TEXT_KEY
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditorActivity : AppCompatActivity() {

  class Contract: ActivityResultContract<Intent, String?>() {
    override fun createIntent(context: Context, input: Intent) = input

    override fun parseResult(resultCode: Int, intent: Intent?)
        = if (resultCode == Activity.RESULT_OK && intent != null) intent.getStringExtra(TEXT_KEY)
    else null
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_fragment)
    if (savedInstanceState == null) {

      supportFragmentManager.beginTransaction()
        .replace(R.id.container, ShellWorkScriptEditorFragment.newInstance(
          initialText = intent.getStringExtra(TEXT_KEY)
        ))
        .commitNow()
    }
  }
}