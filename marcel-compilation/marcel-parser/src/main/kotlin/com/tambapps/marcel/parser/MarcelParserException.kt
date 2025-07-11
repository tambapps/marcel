package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import java.util.stream.Collectors

class MarcelParserException(val errors: List<Error>) : RuntimeException(
  generateErrorMessage(
    errors
  )
) {
  data class Error(val message: String, val eof: Boolean, val token: LexToken)

  @JvmOverloads
  constructor(token: LexToken, message: String, eof: Boolean = false) : this(Error(message, eof, token))

  private constructor(error: Error) : this(listOf<Error>(error))

  val isEof: Boolean
    get() = errors[errors.size - 1].eof

  companion object {
    fun error(message: String, eof: Boolean, token: LexToken): Error {
      return Error(message, eof, token)
    }

    private fun generateErrorMessage(errors: List<Error>): String {
      if (errors.size == 1) return generateErrorMessage(errors[0])
      return "Multiple syntax errors were found:" + errors.stream()
        .map { e: Error ->
          """
  -${generateErrorMessage(e)}"""
        }
        .collect(Collectors.joining())
    }

    private fun generateErrorMessage(error: Error): String {
      return String.format(
        "Syntax error line %d, column %d near token %s: %s",
        error.token.line + 1, error.token.column, error.token.infoString(), error.message
      )
    }

    fun malformedNumber(e: NumberFormatException, token: LexToken, eof: Boolean): Error {
      return Error("Malformed number (" + e.message + ")", eof, token)
    }
  }
}