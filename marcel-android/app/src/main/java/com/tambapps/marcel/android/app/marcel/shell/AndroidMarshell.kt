package com.tambapps.marcel.android.app.marcel.shell

import com.tambapps.marcel.android.app.marcel.shell.jar.DexJarWriterFactory
import com.tambapps.marcel.repl.MarcelShell
import marcel.lang.MarcelClassLoader
import java.io.PrintStream

class AndroidMarshell(
  out: PrintStream,
  marcelClassLoader: MarcelClassLoader,
  private val readLineFunction: (String) -> String
) : MarcelShell(out, marcelClassLoader, DexJarWriterFactory()) {

  override fun readLine(prompt: String): String {
    return readLineFunction.invoke(prompt)
  }
}