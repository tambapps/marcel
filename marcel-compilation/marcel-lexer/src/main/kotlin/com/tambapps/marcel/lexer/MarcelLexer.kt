package com.tambapps.marcel.lexer

/**
 * Marcel lexer.
 * Thread-safe
 *
 * @constructor Create Marcel lexer instance
 */
class MarcelLexer {

  companion object {
    private val COMMENT_TOKENS: List<TokenType> = listOf(
      TokenType.BLOCK_COMMENT, TokenType.DOC_COMMENT, TokenType.HASH, TokenType.SHEBANG_COMMENT, TokenType.EOL_COMMENT
    )

    fun isCommentToken(token: LexToken) = COMMENT_TOKENS.contains(token.type)
  }

  /**
   * perform the lexical analysis on the provided string
   *
   * @param content a string representing Marcel source code
   * @return the list of tokens resulting from the lexical analysis
   */
  @Throws(MarcelLexerException::class)
  fun lex(content: String): List<LexToken> {
    val jflexer = MarcelJflexer()
    jflexer.reset(content, 0, content.length, MarcelJflexer.YYINITIAL)
    val tokens: MutableList<LexToken> = ArrayList()
    var token: LexToken?
    while (true) {
      try {
        token = jflexer.nextToken()
      } catch (e: MarcelJfexerException) {
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
        throw MarcelLexerException(token.line, token.column, "Bad character " + token.value)
      }
      tokens.add(token)
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