package com.tambapps.marcel.android.app.ui.home

import android.view.KeyEvent
import android.view.View
import android.view.View.OnKeyListener
import android.widget.EditText
import android.widget.TextView
import com.tambapps.marcel.android.app.databinding.FragmentHomeBinding
import java.util.concurrent.LinkedBlockingQueue

class PromptKeyListener constructor(
  private val promptQueue: LinkedBlockingQueue<CharSequence>
): OnKeyListener {

  override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
    if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
      val originalText = (v as EditText).text
      promptQueue.add(originalText)
      return true
    }
    return false
  }

}