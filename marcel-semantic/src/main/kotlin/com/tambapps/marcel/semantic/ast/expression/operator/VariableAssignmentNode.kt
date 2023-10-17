package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.variable.Variable

class VariableAssignmentNode(
  val variable: Variable,
  val expression: ExpressionNode,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionNode(variable.type, tokenStart, tokenEnd) {
  constructor(variable: Variable, expression: ExpressionNode, node: CstNode)
      : this(variable, expression, node.tokenStart, node.tokenEnd)

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)

}