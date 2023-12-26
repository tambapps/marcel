package com.tambapps.marcel.semantic.exception

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import java.util.stream.Collectors

open class MarcelSemanticException(errors: List<Error>) : RuntimeException(
  generateErrorMessage(
    errors
  )
) {
  class Error(val message: String, val eof: Boolean, val token: LexToken)

  constructor(node: CstNode, message: String) : this(node.token, message)

  @JvmOverloads
  constructor(token: LexToken, message: String, eof: Boolean = false) : this(Error(message, eof, token))

  private constructor(error: Error) : this(listOf<Error>(error))

  companion object {
    fun error(message: String, eof: Boolean, token: LexToken): Error {
      return Error(message, eof, token)
    }

    private fun generateErrorMessage(errors: List<Error>): String {
      if (errors.size == 1) return generateErrorMessage(errors[0])
      return "Multiple semantic errors were found:" + errors.stream()
        .map { e: Error ->
          """
  -${generateErrorMessage(e)}"""
        }
        .collect(Collectors.joining())
    }

    private fun generateErrorMessage(error: Error): String {
      return String.format(
        "Semantic error line %d, column %d at %s: %s",
        error.token.line + 1, error.token.column, error.token.infoString(), error.message
      )
    }

    fun malformedNumber(e: NumberFormatException, token: LexToken, eof: Boolean): Error {
      return Error("Malformed number (" + e.message + ")", eof, token)
    }
  }
}