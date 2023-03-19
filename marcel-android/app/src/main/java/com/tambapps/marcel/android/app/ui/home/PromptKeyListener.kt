package com.tambapps.marcel.android.app.ui.home

import android.view.KeyEvent
import android.view.View
import android.view.View.OnKeyListener
import android.widget.TextView
import com.tambapps.marcel.android.app.databinding.FragmentHomeBinding
import marcel.lang.printer.Printer
import java.util.concurrent.LinkedBlockingQueue

class PromptKeyListener(
  private val binding: FragmentHomeBinding,
  private val printer: Printer,
  private val promptQueue: LinkedBlockingQueue<String>
): OnKeyListener {

  override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
    if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
      val text = (v as TextView).text.toString()
      promptQueue.add(text)
      printer.println(binding.promptText.text.toString() + " " + text)
      v.text = ""
      return true
    }
    return false
  }

}