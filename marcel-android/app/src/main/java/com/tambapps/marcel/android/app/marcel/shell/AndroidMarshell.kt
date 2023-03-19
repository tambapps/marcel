package com.tambapps.marcel.android.app.marcel.shell

import com.tambapps.marcel.android.app.marcel.shell.jar.DexJarWriterFactory
import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.printer.Printer
import marcel.lang.MarcelClassLoader

class AndroidMarshell(
  out: Printer,
  marcelClassLoader: MarcelClassLoader,
  private val readLineFunction: (String) -> String
) : MarcelShell(out, marcelClassLoader, DexJarWriterFactory(),
  "marshell:%02d>") {

  override fun readLine(prompt: String): String {
    return readLineFunction.invoke(prompt)
  }
}