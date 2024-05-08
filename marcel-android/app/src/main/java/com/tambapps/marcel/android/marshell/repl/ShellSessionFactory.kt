package com.tambapps.marcel.android.marshell.repl

import android.os.Environment
import android.util.Log
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.dumbbell.DumbbellEngine
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.variable.field.BoundField
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Named

class ShellSessionFactory @Inject constructor(
  private val compilerConfiguration: CompilerConfiguration,
  @Named("shellSessionsDirectory")
  private val shellSessionsDirectory: File,
  private val dumbbellEngine: DumbbellEngine
) {

  private val autoIncrement = AtomicInteger()

  fun newSession(): ShellSession {
    Dumbbell.setEngine(dumbbellEngine)
    val sessionDirectory = File(shellSessionsDirectory, "session_" + autoIncrement.incrementAndGet())
    if (!sessionDirectory.isDirectory && !sessionDirectory.mkdirs()) {
      Log.e("ShellSessionFactory", "Couldn't create shell session directory")
      throw RuntimeException("Couldn't create shell session directory")
    }
    val session = ShellSession(compilerConfiguration, sessionDirectory)
    if (Environment.isExternalStorageManager()) {
      val boundField = BoundField(File::class.javaType, "ROOT_DIR", MarshellScript::class.javaType)
      session.symbolResolver.defineBoundField(boundField)
      session.binding.setVariable(boundField.name, Environment.getExternalStorageDirectory())
    }
    return session
  }
}