package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeNode
import com.tambapps.marcel.parser.cst.expression.ExpressionNode

class VariableDeclarationNode(
  val type: TypeNode,
  override val value: String,
  val expressionNode: ExpressionNode?,
  parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken
) :
  AbstractStatementNode(parent, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    if (!super.equals(other)) return false

    other as VariableDeclarationNode

    if (type != other.type) return false
    if (value != other.value) return false
    if (expressionNode != other.expressionNode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + type.hashCode()
    result = 31 * result + value.hashCode()
    result = 31 * result + (expressionNode?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    val s = "$type $value"
    return if (expressionNode != null) "$s = $expressionNode;" else "$s;"
  }
}