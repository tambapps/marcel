package com.tambapps.marcel.android.marshell.repl

import android.os.Environment
import android.util.Log
import com.tambapps.marcel.android.compiler.DexJarWriter
import com.tambapps.marcel.android.marshell.repl.console.NoOpPrinter
import com.tambapps.marcel.android.marshell.repl.console.Printer
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.dumbbell.DumbbellEngine
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.variable.field.BoundField
import marcel.lang.AndroidSystem
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class ShellSessionFactory @Inject constructor(
  private val compilerConfiguration: CompilerConfiguration,
  @Named("shellSessionsDirectory")
  private val shellSessionsDirectory: File,
  @Named("workSessionsDirectory")
  private val workSessionsDirectory: File,
  private val dumbbellEngine: DumbbellEngine,
  @Named("initScriptFile")
  private val initScriptFile: File,
  @Named("shellAndroidSystem")
  private val shellAndroidSystem: AndroidSystem,
  @Named("workoutAndroidSystem")
  private val workoutAndroidSystem: AndroidSystem,
) {

  fun newReplCompiler() = newShellSession(NoOpPrinter).replCompiler

  fun newShellSession(printer: Printer) = newSession(shellSessionsDirectory, printer, shellAndroidSystem)
  fun newWorkSession(printer: Printer) = newSession(workSessionsDirectory, printer, workoutAndroidSystem)

  private fun newSession(sessionsDirectory: File, printer: Printer, androidSystem: AndroidSystem): ShellSession {
    Dumbbell.setEngine(dumbbellEngine) // initialize dumbbell
    val sessionDirectory = File(sessionsDirectory, "session_" + System.currentTimeMillis())
    if (!sessionDirectory.isDirectory && !sessionDirectory.mkdirs()) {
      Log.e("ShellSessionFactory", "Couldn't create shell session directory")
      throw IOException("Couldn't create shell session directory")
    }
    val binding = Binding()
    val classLoader = MarcelDexClassLoader()
    val symbolResolver = ReplMarcelSymbolResolver(classLoader)
    val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, symbolResolver)
    val evaluator = MarshellEvaluator(binding, replCompiler, classLoader, DexJarWriter(), sessionDirectory, printer, androidSystem)

    if (Environment.isExternalStorageManager()) {
      val boundField = BoundField(File::class.javaType, "ROOT_DIR", MarshellScript::class.javaType)
      symbolResolver.defineBoundField(boundField)
      binding.setVariable(boundField.name, Environment.getExternalStorageDirectory())
    }
    val session = ShellSession(symbolResolver, replCompiler, evaluator, printer, sessionDirectory)
    if (initScriptFile.isFile) {
      val text = try {
        initScriptFile.readText()
      } catch (e: Throwable) {
        throw IOException("Couldn't read initialization script: ${e.localizedMessage}")
      }
      if (text.isNotBlank()) {
        try {
          session.eval(text)
        } catch (e: Throwable) {
          Log.e("ShellSessionFactory", "Error while running initialization script: ${e.localizedMessage}", e)
          throw IOException("Error while running initialization script: ${e.localizedMessage}")
        }
      }
    }
    return session
  }

  fun dispose() {
    shellSessionsDirectory.deleteRecursively()
    // not deleting workSessionsDirectory because works might be on-going
  }
}