package com.tambapps.marcel.lexer

/**
 * Marcel lexer
 *
 * @property ignoreWhitespaces
 * @constructor Create empty Marcel lexer
 */
class MarcelLexer @JvmOverloads constructor(private val ignoreWhitespaces: Boolean = true) {

  companion object {
    private val COMMENT_TOKENS: List<TokenType> = listOf(
      TokenType.BLOCK_COMMENT, TokenType.DOC_COMMENT, TokenType.HASH, TokenType.SHEBANG_COMMENT, TokenType.EOL_COMMENT
    )
  }

  /**
   * perform the lexical analysis on the provided string
   *
   * @param content a string representing Marcel source code
   * @return the list of tokens resulting from the lexical analysis
   */
  @Throws(MarcelLexerException::class)
  fun lex(content: String): List<LexToken> {
    return lex(content, false)
  }

  /**
   * perform the lexical analysis safely, without throwing any error. If an error
   * is caught the analysis will just stop there
   *
   * @param content a string representing Marcel source code
   * @return the list of tokens resulting from the lexical analysis
   */
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

}