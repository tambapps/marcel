package com.tambapps.marcel.android.marshell.repl

import com.tambapps.marcel.android.marshell.repl.console.Printer
import com.tambapps.marcel.repl.MarcelEvaluator
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.jar.JarWriter
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import marcel.lang.Script
import marcel.lang.android.AndroidSystem
import marcel.lang.android.AndroidSystemHandler
import java.io.File

class MarshellEvaluator(
  binding: Binding,
  replCompiler: MarcelReplCompiler,
  scriptLoader: MarcelClassLoader,
  jarWriter: JarWriter,
  tempDir: File,
  private val printer: Printer,
  private val androidSystem: AndroidSystemHandler
) : MarcelEvaluator(binding, replCompiler, scriptLoader, jarWriter, tempDir) {

  override fun onScriptLoaded(script: Script) {
    script as MarshellScript
    script.setPrinter(printer)
    AndroidSystem.init(androidSystem)
  }
}