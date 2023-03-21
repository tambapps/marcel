package com.tambapps.marcel.android.app.ui.home

import android.app.Activity
import android.widget.TextView
import marcel.lang.printer.Printer

class TextViewPrinter constructor(private val activity: Activity, private val textView: TextView):
  Printer {
  override fun print(s: CharSequence) {
    activity.runOnUiThread {
      textView.append(s)
    }
  }

  override fun println() {
    print("\n")
  }

  override fun println(s: CharSequence) {
    print("$s\n")
  }
}