package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

class MethodCstNode constructor(
  val parentClassNode: ClassCstNode,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  accessNode: AccessCstNode,
  val name: String,
  val returnTypeNode: TypeCstNode,
  val isReturnTypeNullable: Boolean,
  val isAsync: Boolean = false,
  val isOverride: Boolean? = null, // null means not specified, useful for code generated methods
  identifierToken: LexToken? = null,
) :
  AbstractMethodCstNode(parentClassNode, tokenStart, tokenEnd, accessNode, identifierToken = identifierToken) {

  var isSingleStatementFunction = false // whether if is fun type method() -> statement()



  override fun toString() = StringBuilder().apply {
    append("fun ")
    if (isAsync) append(" async")
    append(returnTypeNode)
    append(" ")
    append(name)
    append(parameters.joinToString(separator = ", ", prefix = "(", postfix = ")"))
  }.toString()
}