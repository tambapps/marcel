package com.tambapps.marcel.android.app.ui.home

import android.app.Activity
import android.widget.TextView
import com.tambapps.marcel.repl.printer.Printer

class TextViewPrinter constructor(private val activity: Activity, private val textView: TextView): Printer {
  override fun print(s: String) {
    activity.runOnUiThread {
      textView.append(s)
    }
  }

  override fun println() {
    print("\n")
  }

  override fun println(o: Any?) {
    println("$o")
  }

  override fun println(s: String) {
    print("$s\n")
  }
}