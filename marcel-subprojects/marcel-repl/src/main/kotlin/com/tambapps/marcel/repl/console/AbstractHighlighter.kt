package com.tambapps.marcel.repl.console

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.extensions.forEach
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver

abstract class AbstractHighlighter<T, Style> constructor(
  private val symbolResolver: MarcelSymbolResolver,
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

    val semanticResult = replCompiler.tryParse(textStr)

    val methodNode = semanticResult?.runMethodNode
    val tokenMap = mutableMapOf<LexToken, Style>()
    // getting ast nodes of interest and mapping them to styles
    methodNode?.blockStatement?.forEach {
      if (it.tokenEnd != LexToken.DUMMY
        && !tokenMap.containsKey(it.tokenStart)) {
        when (it) {
          is ReferenceNode -> tokenMap[it.tokenStart] = variableStyle
          is com.tambapps.marcel.semantic.ast.expression.FunctionCallNode -> tokenMap[it.tokenStart] = functionStyle
        }
      }
    }

    val tokens = semanticResult?.tokens ?: lexer.lexSafely(textStr)

    doHighlight(text, highlightedString, tokens, tokenMap)
    return highlightedString
  }

  // exclusive end
  protected abstract fun highlight(highlightedString: T, style: Style, string: String, startIndex: Int, endIndex: Int)

  private fun doHighlight(text: CharSequence, highlightedString: T, tokens: List<LexToken>,
                          tokenMap: Map<LexToken, Style>) {
    for (i in 0 until tokens.size - 1) { // - 1 to avoid handling END_OF_FILE token
      val token = tokens[i]
      val string = text.substring(token.start, token.end)
      val style = when (token.type) {
        TokenType.IDENTIFIER -> tokenMap[token] ?: defaultStyle
        TokenType.TYPE_INT, TokenType.TYPE_LONG, TokenType.TYPE_SHORT, TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE, TokenType.TYPE_BOOL, TokenType.TYPE_BYTE, TokenType.TYPE_VOID, TokenType.TYPE_CHAR, TokenType.FUN, TokenType.RETURN,
        TokenType.VALUE_TRUE, TokenType.VALUE_FALSE, TokenType.NEW, TokenType.IMPORT, TokenType.AS, TokenType.INLINE, TokenType.STATIC, TokenType.FOR, TokenType.IN, TokenType.IF, TokenType.ELSE, TokenType.NULL, TokenType.BREAK, TokenType.CONTINUE, TokenType.DEF,
        TokenType.CLASS, TokenType.EXTENSION, TokenType.PACKAGE, TokenType.EXTENDS, TokenType.IMPLEMENTS, TokenType.FINAL, TokenType.SWITCH, TokenType.WHEN, TokenType.THIS, TokenType.SUPER, TokenType.DUMBBELL, TokenType.TRY, TokenType.CATCH, TokenType.FINALLY,
        TokenType.INSTANCEOF, TokenType.THROW, TokenType.THROWS, TokenType.CONSTRUCTOR, TokenType.DYNOBJ,
          // visibilities
        TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED, TokenType.VISIBILITY_INTERNAL, TokenType.VISIBILITY_PRIVATE -> keywordStyle
        TokenType.INTEGER, TokenType.FLOAT -> numberStyle
        TokenType.BLOCK_COMMENT, TokenType.DOC_COMMENT, TokenType.HASH, TokenType.SHEBANG_COMMENT, TokenType.EOL_COMMENT -> commentStyle
        TokenType.OPEN_QUOTE, TokenType.CLOSING_QUOTE, TokenType.REGULAR_STRING_PART,
        TokenType.OPEN_CHAR_QUOTE, TokenType.CLOSING_CHAR_QUOTE,
        TokenType.OPEN_REGEX_QUOTE, TokenType.CLOSING_REGEX_QUOTE,
        TokenType.OPEN_SIMPLE_QUOTE, TokenType.CLOSING_SIMPLE_QUOTE -> stringStyle
        TokenType.SHORT_TEMPLATE_ENTRY_START, TokenType.LONG_TEMPLATE_ENTRY_START, TokenType.LONG_TEMPLATE_ENTRY_END -> stringTemplateStyle
        else -> defaultStyle
      }
      highlight(highlightedString, style, string, token.start, token.end)
    }
  }
}