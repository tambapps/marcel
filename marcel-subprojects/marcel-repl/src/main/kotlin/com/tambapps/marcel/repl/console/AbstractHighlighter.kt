package com.tambapps.marcel.repl.console

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.processor.extensions.forEach

abstract class AbstractHighlighter<HighlightedString, Builder, Style> constructor(
  private val replCompiler: MarcelReplCompiler,
  private val style: HighlightTheme<Style>,
) {

  private val lexer = MarcelLexer()

  protected abstract fun newBuilder(): Builder
  protected abstract fun build(builder: Builder): HighlightedString

  fun highlight(text: CharSequence): HighlightedString {
    return build(highlightBuilder(text))
  }

  fun highlightBuilder(text: CharSequence): Builder {
    val builder = newBuilder()
    val textStr = text.toString()

    val semanticResult = replCompiler.tryApplySemantic(textStr)

    val methodNode = semanticResult?.runMethodNode
    val tokenMap = mutableMapOf<LexToken, Style>()
    // getting ast nodes of interest and mapping them to styles
    methodNode?.blockStatement?.forEach {
      if (it.tokenEnd != LexToken.DUMMY
        && !tokenMap.containsKey(it.tokenStart)) {
        when (it) {
          is ReferenceNode -> tokenMap[it.tokenStart] = style.variable
          is FunctionCallNode -> tokenMap[it.tokenStart] = style.function
        }
      }
    }

    val tokens = semanticResult?.tokens ?: tryLex(textStr)

    if (tokens != null) {
      doHighlight(text, builder, tokens, tokenMap)
    } else {
      highlight(builder, style.default, textStr)
    }
    return builder
  }

  private fun tryLex(text: String): List<LexToken>? =
    try { lexer.lex(text) } catch (e: MarcelLexerException) { null }

  // exclusive end
  protected abstract fun highlight(builder: Builder, style: Style, string: String)

  private fun doHighlight(text: CharSequence, builder: Builder, tokens: List<LexToken>,
                          tokenMap: Map<LexToken, Style>) {
    for (i in 0 until tokens.size - 1) { // - 1 to avoid handling END_OF_FILE token
      val token = tokens[i]
      val string = text.substring(token.start, token.end)
      val style = when (token.type) {
        TokenType.IDENTIFIER -> tokenMap[token] ?: style.default
        TokenType.TYPE_INT, TokenType.TYPE_LONG, TokenType.TYPE_SHORT, TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE, TokenType.TYPE_BOOL, TokenType.TYPE_BYTE, TokenType.TYPE_VOID, TokenType.TYPE_CHAR, TokenType.FUN, TokenType.RETURN,
        TokenType.VALUE_TRUE, TokenType.VALUE_FALSE, TokenType.NEW, TokenType.IMPORT, TokenType.AS, TokenType.INLINE, TokenType.STATIC, TokenType.FOR, TokenType.IN, TokenType.IF, TokenType.ELSE, TokenType.NULL, TokenType.BREAK, TokenType.CONTINUE, TokenType.DEF,
        TokenType.CLASS, TokenType.EXTENSION, TokenType.PACKAGE, TokenType.EXTENDS, TokenType.IMPLEMENTS, TokenType.FINAL, TokenType.SWITCH, TokenType.WHEN, TokenType.THIS, TokenType.SUPER, TokenType.DUMBBELL, TokenType.TRY, TokenType.CATCH, TokenType.FINALLY,
        TokenType.INSTANCEOF, TokenType.THROW, TokenType.THROWS, TokenType.CONSTRUCTOR, TokenType.DYNOBJ,
          // visibilities
        TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED, TokenType.VISIBILITY_INTERNAL, TokenType.VISIBILITY_PRIVATE -> style.keyword
        TokenType.INTEGER, TokenType.FLOAT -> style.number
        TokenType.BLOCK_COMMENT, TokenType.DOC_COMMENT, TokenType.HASH, TokenType.SHEBANG_COMMENT, TokenType.EOL_COMMENT -> style.comment
        TokenType.OPEN_QUOTE, TokenType.CLOSING_QUOTE, TokenType.REGULAR_STRING_PART,
        TokenType.OPEN_CHAR_QUOTE, TokenType.CLOSING_CHAR_QUOTE,
        TokenType.OPEN_REGEX_QUOTE, TokenType.CLOSING_REGEX_QUOTE,
        TokenType.OPEN_SIMPLE_QUOTE, TokenType.CLOSING_SIMPLE_QUOTE -> style.string
        TokenType.SHORT_TEMPLATE_ENTRY_START, TokenType.LONG_TEMPLATE_ENTRY_START, TokenType.LONG_TEMPLATE_ENTRY_END -> style.stringTemplate
        else -> style.default
      }
      highlight(builder, style, string)
    }
  }
}