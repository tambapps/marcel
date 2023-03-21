package com.tambapps.marcel.android.app.marcel.shell

import android.os.Build
import com.tambapps.marcel.android.app.marcel.shell.jar.DexJarWriterFactory
import com.tambapps.marcel.repl.MarcelShell
import marcel.lang.MarcelClassLoader
import marcel.lang.printer.Printer
import marcel.lang.util.MarcelVersion

class AndroidMarshell(
  out: Printer,
  marcelClassLoader: MarcelClassLoader,
  private val readLineFunction: (String) -> String
) : MarcelShell(out, marcelClassLoader, DexJarWriterFactory(),
  "marshell:%02d> ") {

  override fun readLine(prompt: String): String {
    return readLineFunction.invoke(prompt)
  }

  override fun printVersion() {
    printer.print("Marshell (Marcel: ${MarcelVersion.VERSION}, Android ${Build.VERSION.RELEASE})")
  }

  fun newHighlighter(): TextViewHighlighter {
    return TextViewHighlighter(typeResolver, replCompiler)
  }
}