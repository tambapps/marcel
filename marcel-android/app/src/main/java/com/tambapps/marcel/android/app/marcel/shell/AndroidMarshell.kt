package com.tambapps.marcel.android.app.marcel.shell

import android.os.Build
import com.tambapps.marcel.android.app.marcel.shell.jar.DexJarWriterFactory
import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.printer.SuspendPrinter
import marcel.lang.MarcelClassLoader
import marcel.lang.util.MarcelVersion

class AndroidMarshell(
  out: SuspendPrinter,
  marcelClassLoader: MarcelClassLoader,
  private val readLineFunction: suspend (String) -> String
) : MarcelShell(out, marcelClassLoader, DexJarWriterFactory(),
  "marshell:%02d> ") {

  override suspend fun readLine(prompt: String): String {
    return readLineFunction.invoke(prompt)
  }

  override suspend fun printVersion() {
    printer.suspendPrintln("Marshell (Marcel: ${MarcelVersion.VERSION}, Android ${Build.VERSION.RELEASE})")
  }

  fun newHighlighter(): TextViewHighlighter {
    return TextViewHighlighter(typeResolver, replCompiler)
  }

  override suspend fun printEval(eval: Any?) {
    printer.suspendPrintln(eval)
  }
}