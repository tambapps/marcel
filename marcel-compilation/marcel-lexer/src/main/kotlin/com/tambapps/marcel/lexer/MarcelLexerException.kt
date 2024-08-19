package com.tambapps.marcel.lexer


/**
 * Marcel lexer exception
 *
 * @property line the line at which the error did occur
 * @property column the column at which the error did occur
 * @constructor
 *
 * @param message the message of the error
 */
class MarcelLexerException constructor(val line: Int, val column: Int, message: String?) :
  RuntimeException(String.format("Lexer error at line %d, column %d: %s", line + 1, column, message))
