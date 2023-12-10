package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.statement.StatementNode

sealed class AbstractMethodNode(parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken,
                                val accessNode: AccessNode) :
  AbstractCstNode(parent, tokenStart, tokenEnd) {

  val parameters = mutableListOf<MethodParameterCstNode>()
  val annotations = mutableListOf<AnnotationNode>()
  val statements = mutableListOf<StatementNode>()
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AbstractMethodNode) return false
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