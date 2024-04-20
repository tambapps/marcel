package com.tambapps.marcel.android.marshell.repl

import com.tambapps.marcel.compiler.CompilerConfiguration
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Named

class ShellSessionFactory @Inject constructor(
    private val compilerConfiguration: CompilerConfiguration,
    @Named("shellSessionsDirectory")
    private val shellSessionsDirectory: File
) {

  private val autoIncrement = AtomicInteger()

  fun newSession(): ShellSession {
    val sessionDirectory = File(shellSessionsDirectory, "session_" + autoIncrement.incrementAndGet())
    if (!sessionDirectory.isDirectory && !sessionDirectory.mkdirs()) {
      throw RuntimeException("Uh ohhhhhhhhh") // TODO handle failure better
    }
    return ShellSession(compilerConfiguration, sessionDirectory)
  }
}