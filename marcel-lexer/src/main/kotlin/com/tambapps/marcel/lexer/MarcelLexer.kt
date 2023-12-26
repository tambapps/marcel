package com.tambapps.marcel.lexer

import java.util.*


class MarcelLexer @JvmOverloads constructor(private val ignoreWhitespaces: Boolean = true) {
  @Throws(MarcelLexerException::class)
  fun lex(content: String): List<LexToken> {
    return lex(content, false)
  }

  @Throws(MarcelLexerException::class)
  fun lexSafely(content: String): List<LexToken> {
    return lex(content, true)
  }

  @Throws(MarcelLexerException::class)
  private fun lex(content: String, catchAndStop: Boolean): List<LexToken> {
    val jflexer = MarcelJflexer()
    jflexer.reset(content, 0, content.length, MarcelJflexer.YYINITIAL)
    val tokens: MutableList<LexToken> = ArrayList()
    var token: LexToken?
    while (true) {
      try {
        token = jflexer.nextToken()
      } catch (e: MarcelJfexerException) {
        if (catchAndStop) {
          return tokens
        }
        val line = jflexer.yyline
        val column = jflexer.yycolumn
        var message = e.message
        if (jflexer.zzLexicalState == MarcelJflexer.STRING || jflexer.zzLexicalState == MarcelJflexer.SIMPLE_STRING || jflexer.zzLexicalState == MarcelJflexer.CHAR_STRING || jflexer.zzLexicalState == MarcelJflexer.RAW_STRING || jflexer.zzLexicalState == MarcelJflexer.REGEX_STRING) {
          message = "String is malformed"
        }
        throw MarcelLexerException(line, column, message)
      }
      if (token == null) break
      if (token.type == TokenType.BAD_CHARACTER) {
        if (catchAndStop) {
          return tokens
        }
        throw MarcelLexerException(token.line, token.column, "Bad character " + token.value)
      }
      if (!COMMENT_TOKENS.contains(token.type) && (!ignoreWhitespaces || token.type != TokenType.WHITE_SPACE)) {
        tokens.add(token)
      }
    }
    tokens.add(
      LexToken(
        content.length, content.length,
        jflexer.yyline, jflexer.yycolumn, TokenType.END_OF_FILE, null
      )
    )
    return tokens
  }

  companion object {
    private val COMMENT_TOKENS: List<TokenType> = Arrays.asList(
      TokenType.BLOCK_COMMENT, TokenType.DOC_COMMENT, TokenType.HASH, TokenType.SHEBANG_COMMENT, TokenType.EOL_COMMENT
    )
  }
}