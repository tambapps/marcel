package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.statement.StatementCstNode

sealed class AbstractMethodCstNode(parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken,
                                   val accessNode: AccessCstNode) :
  AbstractCstNode(parent, tokenStart, tokenEnd) {

  var isVarArgs = false
  val parameters = mutableListOf<MethodParameterCstNode>()
  val annotations = mutableListOf<AnnotationCstNode>()
  val statements = mutableListOf<StatementCstNode>()
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AbstractMethodCstNode) return false
    if (!super.equals(other)) return false

    if (parameters != other.parameters) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + parameters.hashCode()
    return result
  }
}