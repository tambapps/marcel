package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException

/**
 * Util class to parse dumbbells
 */
object DumbbellsParser {

  fun parse(text: String) = parse(MarcelLexer().lex(text))

  fun parse(tokens: List<LexToken>): List<String> = try {
    MarcelParser(tokens).dumbbells()
  } catch (e: Exception) {
    if (e is MarcelLexerException || e is MarcelParserException) emptyList()
    else throw e
  }
}