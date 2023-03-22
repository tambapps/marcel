package com.tambapps.marcel.android.app.ui.shell

import android.app.Activity
import android.widget.TextView
import com.tambapps.marcel.repl.printer.SuspendPrinter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import marcel.lang.printer.Printer

class TextViewPrinter constructor(private val activity: Activity, private val textView: TextView):
  SuspendPrinter, Printer {
  override suspend fun suspendPrint(s: CharSequence?) {
    withContext(Dispatchers.Main) {
      textView.append(s)
    }
  }

  override suspend fun suspendPrintln() {
    suspendPrint("\n")
  }

  override suspend fun suspendPrintln(s: CharSequence?) {
    suspendPrint(s)
    suspendPrintln()
  }

  // activity is needed for this, as this is for print from marcel scripts
  override fun print(p0: CharSequence?) {
    activity.runOnUiThread {
      textView.append(p0)
    }

  }

  override fun println(p0: CharSequence?) {
    print(p0)
    print("\n")
  }
  override fun println() {
    print("\n")
  }
}