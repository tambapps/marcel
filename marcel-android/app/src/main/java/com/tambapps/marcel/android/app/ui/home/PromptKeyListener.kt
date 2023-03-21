package com.tambapps.marcel.android.app.ui.home

import android.view.KeyEvent
import android.view.View
import android.view.View.OnKeyListener
import android.widget.TextView
import com.tambapps.marcel.android.app.databinding.FragmentHomeBinding
import marcel.lang.printer.Printer
import java.util.concurrent.LinkedBlockingQueue

class PromptKeyListener constructor(
  private val binding: FragmentHomeBinding,
  private val printer: Printer,
  private val promptQueue: LinkedBlockingQueue<String>
): OnKeyListener {

  override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
    if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
      val originalText = (v as TextView).text.toString()
      promptQueue.add(originalText)
      // TODO test highlighter
      printer.println("\n" + binding.promptText.text.toString() + " " + originalText)
      v.text = ""
      return true
    }
    return false
  }

}