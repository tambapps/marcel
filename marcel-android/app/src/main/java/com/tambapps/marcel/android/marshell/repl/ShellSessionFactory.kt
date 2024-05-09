package com.tambapps.marcel.android.marshell.repl

import android.os.Environment
import android.util.Log
import com.tambapps.marcel.android.marshell.repl.jar.DexJarWriterFactory
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.dumbbell.DumbbellEngine
import com.tambapps.marcel.repl.MarcelEvaluator
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.variable.field.BoundField
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
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
    val binding = Binding()
    val classLoader = MarcelDexClassLoader()
    val symbolResolver = ReplMarcelSymbolResolver(classLoader, binding)
    val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, symbolResolver)
    val evaluator = MarcelEvaluator(binding, replCompiler, classLoader, DexJarWriterFactory(), sessionDirectory)

    if (Environment.isExternalStorageManager()) {
      val boundField = BoundField(File::class.javaType, "ROOT_DIR", MarshellScript::class.javaType)
      symbolResolver.defineBoundField(boundField)
      binding.setVariable(boundField.name, Environment.getExternalStorageDirectory())
    }
    return ShellSession(symbolResolver, replCompiler, evaluator)
  }
}