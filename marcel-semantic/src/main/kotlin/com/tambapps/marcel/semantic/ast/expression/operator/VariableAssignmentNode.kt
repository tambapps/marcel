package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.Variable

class VariableAssignmentNode(
  val variable: Variable,
  val expression: ExpressionNode,
  val owner: ExpressionNode? = null,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionNode(variable.type, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

  constructor(variable: LocalVariable, expression: ExpressionNode, node: CstNode)
      : this(variable, expression, null, node)
  constructor(variable: LocalVariable, expression: ExpressionNode, tokenStart: LexToken,
              tokenEnd: LexToken)
      : this(variable, expression, null, tokenStart, tokenEnd)

  constructor(variable: Variable, expression: ExpressionNode, owner: ExpressionNode? = null, node: CstNode)
      : this(variable, expression, owner, node.tokenStart, node.tokenEnd)

}