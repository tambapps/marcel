package com.tambapps.marcel.android.marshell.repl

import android.os.Environment
import android.util.Log
import com.tambapps.marcel.android.compiler.DexJarWriter
import com.tambapps.marcel.android.marshell.os.AndroidNotifier
import com.tambapps.marcel.android.marshell.os.AndroidSystemImpl
import com.tambapps.marcel.android.marshell.repl.console.NoOpPrinter
import com.tambapps.marcel.android.marshell.repl.console.Printer
import com.tambapps.marcel.android.marshell.service.PreferencesDataStore
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.dumbbell.DumbbellEngine
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.symbol.variable.field.BoundField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import marcel.lang.android.AndroidSystem
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import marcel.lang.android.AndroidSystemHandler
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
  private val preferencesDataStore: PreferencesDataStore,
  @Named("initScriptFile")
  private val initScriptFile: File,
  private val androidSystem: AndroidSystemHandler
) {

  fun newReplCompiler() = newShellSession(NoOpPrinter).replCompiler

  fun newShellSession(printer: Printer) = newSession(shellSessionsDirectory, printer)

  fun newWorkSession(printer: Printer) = newSession(workSessionsDirectory, printer)

  private fun newSession(sessionsDirectory: File, printer: Printer): ShellSession {
    Dumbbell.setEngine(dumbbellEngine) // initialize dumbbell
    val sessionDirectory = File(sessionsDirectory, "session_" + System.currentTimeMillis())
    if (!sessionDirectory.isDirectory && !sessionDirectory.mkdirs()) {
      Log.e("ShellSessionFactory", "Couldn't create shell session directory")
      throw IOException("Couldn't create shell session directory")
    }
    val binding = Binding()
    val classLoader = MarcelDexClassLoader()
    val symbolResolver = MarshellSymbolResolver(classLoader)
    val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, symbolResolver)
    val evaluator = MarshellEvaluator(binding, replCompiler, classLoader, DexJarWriter(), sessionDirectory, printer, androidSystem)

    if (Environment.isExternalStorageManager()) {
      addBoundField(symbolResolver, binding, BoundField(File::class.javaType, "ROOT_DIR", MarshellScript::class.javaType), Environment.getExternalStorageDirectory())
      val homeDirectory = runBlocking { preferencesDataStore.homeDirectory.first() }
      if (homeDirectory != null) {
        addBoundField(symbolResolver, binding, BoundField(File::class.javaType, "HOME_DIR", MarshellScript::class.javaType), homeDirectory)
      }
    }
    val session = ShellSession(symbolResolver, replCompiler, evaluator, printer, sessionDirectory)
    session.imports.wildcardTypeImportPrefixes.add("marcel.lang.android")
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

  private fun addBoundField(symbolResolver: MarshellSymbolResolver, binding: Binding, field: BoundField, value: Any) {
    symbolResolver.defineField(field)
    binding.setVariable(field.name, value)
  }

  fun dispose() {
    shellSessionsDirectory.deleteRecursively()
    // not deleting workSessionsDirectory because works might be on-going
  }
}