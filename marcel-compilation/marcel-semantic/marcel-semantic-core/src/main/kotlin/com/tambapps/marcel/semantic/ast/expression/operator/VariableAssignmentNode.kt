package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AstVariableNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable
import com.tambapps.marcel.semantic.symbol.variable.Variable

class VariableAssignmentNode(
  override var variable: Variable,
  val expression: ExpressionNode,
  val owner: ExpressionNode? = null,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val identifierToken: LexToken? = null
) : AbstractExpressionNode(variable.type, tokenStart, tokenEnd),
  AstVariableNode {
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  constructor(
    localVariable: LocalVariable,
    expression: ExpressionNode,
    node: CstNode,
    identifierToken: LexToken? = null
  )
      : this(localVariable, expression, null, node.tokenStart, node.tokenEnd, identifierToken)

  constructor(
    localVariable: LocalVariable,
    expression: ExpressionNode,
    tokenStart: LexToken,
    tokenEnd: LexToken
  )
      : this(localVariable, expression, null, tokenStart, tokenEnd)

  constructor(
    variable: Variable,
    expression: ExpressionNode,
    owner: ExpressionNode? = null,
    node: CstNode
  )
      : this(variable, expression, owner, node.tokenStart, node.tokenEnd)

  constructor(
    variable: Variable,
    expression: ExpressionNode,
    owner: ExpressionNode? = null
  )
      : this(variable, expression, owner, expression.tokenStart, expression.tokenEnd)

  override fun toString() = if (owner != null) "${owner}.$variable = $expression" else "$variable = $expression"
}