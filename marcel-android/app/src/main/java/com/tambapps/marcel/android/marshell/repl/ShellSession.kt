package com.tambapps.marcel.android.marshell.repl

import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.compiler.exception.MarcelCompilerException
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import java.io.File

class ShellSession(
  internal val symbolResolver: ReplMarcelSymbolResolver,
  internal val replCompiler: MarcelReplCompiler,
  internal val evaluator: MarshellEvaluator,
  val classesDirectory: File,
) {

  companion object {
    fun isMarcelCompilerException(throwable: Throwable) = throwable is MarcelLexerException || throwable is MarcelParserException || throwable is MarcelSemanticException || throwable is MarcelCompilerException
  }

  val binding get() = evaluator.binding
  val functions get() = evaluator.definedFunctions
  val definedTypes get() = evaluator.definedTypes
  val imports get() = evaluator.imports
  val dumbbells get() = evaluator.collectedDumbbells

  fun eval(text: String) = evaluator.eval(text)

  fun evalAsResult(text: String) = runCatching { evaluator.eval(text) }

  fun newHighlighter() = SpannableHighlighter(symbolResolver, replCompiler)
}