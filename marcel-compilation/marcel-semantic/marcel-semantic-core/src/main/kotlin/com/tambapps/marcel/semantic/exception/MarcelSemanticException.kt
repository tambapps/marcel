package com.tambapps.marcel.semantic.exception

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.ModuleNode
import java.util.stream.Collectors

open class MarcelSemanticException constructor(val errors: List<Error>, val ast: ModuleNode? = null) : RuntimeException(
  generateErrorMessage(
    errors
  )
) {
  class Error(val message: String, val token: LexToken)

  constructor(node: CstNode, message: String) : this(node.token, message)

  @JvmOverloads
  constructor(token: LexToken, message: String) : this(Error(message, token))

  private constructor(error: Error) : this(listOf<Error>(error))

  companion object {
    fun error(message: String, token: LexToken): Error {
      return Error(message, token)
    }

    private fun generateErrorMessage(errors: List<Error>): String {
      if (errors.size == 1) return generateErrorMessage(errors[0])
      return "Multiple semantic errors were found:" + errors.stream()
        .map { e: Error ->
          """
  -line ${e.token.line}, column ${e.token.column} near ${e.token.infoString()}: ${e.message}"""
        }
        .collect(Collectors.joining())
    }

    private fun generateErrorMessage(error: Error): String {
      return String.format(
        "Semantic error line %d, column %d near %s: %s",
        error.token.line + 1, error.token.column, error.token.infoString(), error.message
      )
    }

    fun malformedNumber(e: NumberFormatException, token: LexToken): Error {
      return Error("Malformed number (" + e.message + ")", token)
    }
  }
}