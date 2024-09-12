package com.tambapps.marcel.android.marshell.repl

import com.tambapps.marcel.android.marshell.repl.console.Printer
import com.tambapps.marcel.android.marshell.repl.console.SpannableHighlighter
import com.tambapps.marcel.compiler.exception.MarcelCompilerException
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.DumbbellsParser
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import java.io.File

class ShellSession constructor(
  internal val symbolResolver: MarshellSymbolResolver,
  internal val replCompiler: MarcelReplCompiler,
  private val evaluator: MarshellEvaluator,
  private val printer: Printer,
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

  fun eval(text: String): Any? {
    // first making sure all dumbbells are pulled, because it is a heavy process that can freeze the UI if
    //  it's done while compiling
    val dumbbells = DumbbellsParser.parse(text)
    dumbbells.forEach { dumbbell ->
      if (!Dumbbell.isPulled(dumbbell)) {
        printer.print("Dumbbell '$dumbbell' was not found in local repository. Pulling it...")
        Dumbbell.pull(dumbbell)
      }
    }
    // then evaluating
    return evaluator.eval(text)
  }

  fun evalAsResult(text: String) = runCatching { eval(text) }

  fun newHighlighter() = SpannableHighlighter(replCompiler)
}