package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

class VariableDeclarationCstNode constructor(
  val type: TypeCstNode,
  val variableToken: LexToken,
  val expressionNode: ExpressionCstNode?,
  val isNullable: Boolean,
  parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken
) :
  AbstractStatementCstNode(parent, tokenStart, tokenEnd) {

  override val value: String = variableToken.value

  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)

  override fun toString(): String {
    val s = "${type.toString(isNullable)} $value"
    return if (expressionNode != null) "$s = $expressionNode;" else "$s;"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is VariableDeclarationCstNode) return false

    if (isNullable != other.isNullable) return false
    if (type != other.type) return false
    // because we only care about the name
    if (variableToken.value != other.variableToken.value) return false
    if (expressionNode != other.expressionNode) return false
    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    var result = isNullable.hashCode()
    result = 31 * result + type.hashCode()
    result = 31 * result + variableToken.value.hashCode()
    result = 31 * result + (expressionNode?.hashCode() ?: 0)
    result = 31 * result + value.hashCode()
    return result
  }
}