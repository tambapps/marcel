package com.tambapps.marcel.android.marshell.repl

import android.util.Log
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
      Log.e("ShellSessionFactory", "Couldn't create shell session directory")
      throw RuntimeException("Couldn't create shell session directory")
    }
    return ShellSession(compilerConfiguration, sessionDirectory)
  }
}