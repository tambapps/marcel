package com.tambapps.marcel.android.marshell.repl

import android.content.Context
import android.os.Environment
import android.util.Log
import com.tambapps.marcel.android.marshell.repl.console.NoOpPrinter
import com.tambapps.marcel.android.marshell.repl.console.Printer
import com.tambapps.marcel.android.marshell.repl.jar.DexJarWriterFactory
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.dumbbell.DumbbellEngine
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.variable.field.BoundField
import dagger.hilt.android.qualifiers.ApplicationContext
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class ShellSessionFactory @Inject constructor(
  private val compilerConfiguration: CompilerConfiguration,
  @ApplicationContext private val context: Context,
  @Named("shellSessionsDirectory")
  private val shellSessionsDirectory: File,
  private val dumbbellEngine: DumbbellEngine,
  @Named("initScriptFile")
  private val initScriptFile: File,
) {

  fun newSymbolResolverAndCompiler(): Pair<ReplMarcelSymbolResolver, MarcelReplCompiler> {
    return newSession(NoOpPrinter).let {
      Pair(it.symbolResolver, it.replCompiler)
    }
  }
  fun newSession(printer: Printer): ShellSession {
    Dumbbell.setEngine(dumbbellEngine) // initialize dumbbell
    val sessionDirectory = File(shellSessionsDirectory, "session_" + System.currentTimeMillis())
    if (!sessionDirectory.isDirectory && !sessionDirectory.mkdirs()) {
      Log.e("ShellSessionFactory", "Couldn't create shell session directory")
      throw IOException("Couldn't create shell session directory")
    }
    val binding = Binding()
    val classLoader = MarcelDexClassLoader()
    val symbolResolver = ReplMarcelSymbolResolver(classLoader)
    val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, symbolResolver)
    val evaluator = MarshellEvaluator(binding, replCompiler, classLoader, DexJarWriterFactory(), sessionDirectory, printer)

    if (Environment.isExternalStorageManager()) {
      val boundField = BoundField(File::class.javaType, "ROOT_DIR", MarshellScript::class.javaType)
      symbolResolver.defineBoundField(boundField)
      binding.setVariable(boundField.name, Environment.getExternalStorageDirectory())
    }
    val session = ShellSession(symbolResolver, replCompiler, evaluator)
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
          throw IOException("Error while running initialization script: ${e.localizedMessage}")
        }
      }
    }
    return session
  }
}