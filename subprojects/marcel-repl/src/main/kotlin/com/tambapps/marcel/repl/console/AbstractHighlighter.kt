package com.tambapps.marcel.repl.console

import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.semantic.type.JavaTypeResolver

abstract class AbstractHighlighter<T, Style> constructor(
  private val typeResolver: JavaTypeResolver,
  private val replCompiler: MarcelReplCompiler
) {

  private val lexer = MarcelLexer(false)
  
  abstract val variableStyle: Style
  abstract val functionStyle: Style
  abstract val stringStyle: Style
  abstract val stringTemplateStyle: Style
  abstract val keywordStyle: Style
  abstract val commentStyle: Style
  abstract val numberStyle: Style
  abstract val defaultStyle: Style

  abstract fun newHighlightedString(text: CharSequence): T

  fun highlight(text: CharSequence): T {
    val highlightedString = newHighlightedString(text)
    val textStr = text.toString()
    val parseResult = replCompiler.tryParse(textStr)
    val tokens = parseResult?.tokens?.toMutableList() ?: lexer.lexSafely(textStr)
    tokens.removeLastOrNull() // remove end of file
    val scriptNode = parseResult?.scriptNode ?: replCompiler.semanticResult?.scriptNode
    val node = scriptNode?.methods?.find { it.name == "run" && it.parameters.size == 1 }

    // TODO
    return highlightedString
  }

  protected abstract fun highlight(highlightedString: T, style: Style, string: String, startIndex: Int,
                                   // exclusive
                                   endIndex: Int)

}