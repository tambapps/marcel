package com.tambapps.marcel.android.marshell.repl

import com.tambapps.marcel.android.marshell.ui.component.Prompt
import com.tambapps.marcel.android.marshell.repl.jar.DexJarWriterFactory
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.MarcelEvaluator
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import java.io.File

class ShellSession(compilerConfiguration: CompilerConfiguration, classesDir: File) {

  private val coroutineScope = CoroutineScope(Dispatchers.IO)
  private val binding = Binding()
  private val classLoader = MarcelDexClassLoader()
  private val symbolResolver = ReplMarcelSymbolResolver(classLoader, binding)
  private val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, symbolResolver)
  private val evaluator = MarcelEvaluator(binding, replCompiler, classLoader, DexJarWriterFactory(), classesDir)

  fun eval(text: String) = evaluator.eval(text)

  fun eval(text: String, callback: (Prompt.Type, Any?) -> Unit) {
    coroutineScope.launch {
      val result = runCatching { eval(text) }
      withContext(Dispatchers.Main) {
        if (result.isSuccess) {
          callback.invoke(Prompt.Type.SUCCESS_OUTPUT, result.getOrNull())
        } else {
          callback.invoke(Prompt.Type.ERROR_OUTPUT, result.exceptionOrNull())
        }
      }
    }
  }
}