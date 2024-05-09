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

class ShellSession(
  private val symbolResolver: ReplMarcelSymbolResolver,
  private val replCompiler: MarcelReplCompiler,
  private val evaluator: MarcelEvaluator
) {

  companion object {
    fun isMarcelCompilerException(throwable: Throwable) = throwable is MarcelLexerException || throwable is MarcelParserException || throwable is MarcelSemanticException || throwable is MarcelCompilerException

  }

  var scriptConfigurer: ((marcel.lang.Script) -> Unit)?
    get() = evaluator.scriptConfigurer
    set(value) {
      evaluator.scriptConfigurer = value
    }

  fun evalAsResult(text: String) = runCatching { evaluator.eval(text) }

  fun newHighlighter() = SpannableHighlighter(symbolResolver, replCompiler)
}