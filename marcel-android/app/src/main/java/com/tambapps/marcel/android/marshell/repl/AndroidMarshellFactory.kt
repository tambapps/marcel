package com.tambapps.marcel.android.marshell.repl

import com.tambapps.marcel.android.marshell.data.ShellSession
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
  @Named("initScriptFile")
  _initScriptFile: File
  ) {

  private val compilerConfiguration = _compilerConfiguration
  private val initScriptFile = _initScriptFile


  fun newShellRunner(session: ShellSession, printer: SuspendPrinter, lineReader: suspend (String) -> String,
  exitFunc: () -> Unit): AndroidMarshellRunner {
    return AndroidMarshellRunner(
      session,
      AndroidMarshell(compilerConfiguration, session.directory, initScriptFile, printer, exitFunc, session.classLoader,
        session.binding, session.typeResolver, lineReader, session.history)
    )
  }

}