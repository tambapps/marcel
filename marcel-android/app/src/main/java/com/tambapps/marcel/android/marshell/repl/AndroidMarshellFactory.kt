package com.tambapps.marcel.android.marshell.repl

import com.tambapps.marcel.android.marshell.AndroidMarshell
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.printer.SuspendPrinter
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import java.io.File
import javax.inject.Inject
import javax.inject.Named


class AndroidMarshellFactory @Inject constructor(
  // these are not vals because hilt doesn't allow final fields when injecting
  _compilerConfiguration: CompilerConfiguration,
  @Named("classesDir")
  _classesDir: File,
  @Named("initScriptFile")
  _initScriptFile: File
  ) {

  private val compilerConfiguration = _compilerConfiguration
  private val classesDir = _classesDir
  private val initScriptFile = _initScriptFile


  fun newShell(printer: SuspendPrinter, binding: Binding, lineReader: suspend (String) -> String): AndroidMarshell {
    // not a bean because we want to keep them independent per fragment
    val marcelDexClassLoader = MarcelDexClassLoader()
    return AndroidMarshell(compilerConfiguration, classesDir, initScriptFile, printer, marcelDexClassLoader,
      binding, lineReader)
  }

}