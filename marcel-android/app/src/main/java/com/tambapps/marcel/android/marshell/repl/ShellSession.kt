package com.tambapps.marcel.android.marshell.repl

import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.android.marshell.repl.jar.DexJarWriterFactory
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.exception.MarcelCompilerException
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.repl.MarcelEvaluator
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import java.io.File

class ShellSession(compilerConfiguration: CompilerConfiguration, classesDir: File) {

  companion object {
    fun isMarcelCompilerException(throwable: Throwable) = throwable is MarcelLexerException || throwable is MarcelParserException || throwable is MarcelSemanticException || throwable is MarcelCompilerException

  }
  private val coroutineScope = CoroutineScope(Dispatchers.IO)
  internal val binding = Binding()
  private val classLoader = MarcelDexClassLoader()
  internal val symbolResolver = ReplMarcelSymbolResolver(classLoader, binding)
  private val replCompiler = MarcelReplCompiler(compilerConfiguration, classLoader, symbolResolver)
  private val evaluator = MarcelEvaluator(binding, replCompiler, classLoader, DexJarWriterFactory(), classesDir)

  var scriptConfigurer: ((marcel.lang.Script) -> Unit)?
    get() = evaluator.scriptConfigurer
    set(value) {
      evaluator.scriptConfigurer = value
    }

  fun evalAsResult(text: String) = runCatching { evaluator.eval(text) }

  fun newHighlighter() = SpannableHighlighter(symbolResolver, replCompiler)
}