package com.tambapps.marcel.android.marshell.marcel.shell

import android.os.Build
import com.tambapps.marcel.android.marshell.marcel.shell.jar.DexJarWriterFactory
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.printer.SuspendPrinter
import marcel.lang.MarcelClassLoader
import marcel.lang.util.MarcelVersion
import java.io.File

class AndroidMarshell constructor(
  compilerConfiguration: CompilerConfiguration,
  classesDir: File,
  override val initScriptFile: File?,
  out: SuspendPrinter,
  marcelClassLoader: MarcelClassLoader,
  private val readLineFunction: suspend (String) -> String
) : MarcelShell(compilerConfiguration, out, marcelClassLoader, DexJarWriterFactory(),
  classesDir, "%03d> ") {

  override suspend fun readLine(prompt: String): String {
    return readLineFunction.invoke(prompt)
  }

  override suspend fun printVersion() {
    printer.suspendPrintln("Marshell (Marcel: ${MarcelVersion.VERSION}, Android ${Build.VERSION.RELEASE})\n")
  }

  fun newHighlighter(): TextViewHighlighter {
    return TextViewHighlighter(typeResolver, replCompiler)
  }

  override suspend fun printEval(eval: Any?) {
    printer.suspendPrintln(eval)
  }
}