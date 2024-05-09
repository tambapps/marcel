package com.tambapps.marcel.android.marshell.repl

import com.tambapps.marcel.android.marshell.repl.console.Printer
import com.tambapps.marcel.repl.MarcelEvaluator
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.jar.JarWriterFactory
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import marcel.lang.Script
import java.io.File

class MarshellEvaluator(
  binding: Binding,
  replCompiler: MarcelReplCompiler,
  scriptLoader: MarcelClassLoader,
  jarWriterFactory: JarWriterFactory,
  tempDir: File,
  private val printer: Printer
) : MarcelEvaluator(binding, replCompiler, scriptLoader, jarWriterFactory, tempDir) {

  override fun onScriptLoaded(script: Script) {
    (script as MarshellScript).setPrinter(printer)
  }
}