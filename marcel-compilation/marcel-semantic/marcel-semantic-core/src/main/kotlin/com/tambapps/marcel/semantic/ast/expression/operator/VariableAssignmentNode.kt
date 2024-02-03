package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AstVariableNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.Variable

class VariableAssignmentNode(
  override var variable: Variable,
  val expression: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
  val owner: com.tambapps.marcel.semantic.ast.expression.ExpressionNode? = null,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(variable.type, tokenStart, tokenEnd), AstVariableNode {
  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

  constructor(localVariable: LocalVariable, expression: com.tambapps.marcel.semantic.ast.expression.ExpressionNode, node: CstNode)
      : this(localVariable, expression, null, node)
  constructor(localVariable: LocalVariable, expression: com.tambapps.marcel.semantic.ast.expression.ExpressionNode, tokenStart: LexToken,
              tokenEnd: LexToken)
      : this(localVariable, expression, null, tokenStart, tokenEnd)

  constructor(variable: Variable, expression: com.tambapps.marcel.semantic.ast.expression.ExpressionNode, owner: com.tambapps.marcel.semantic.ast.expression.ExpressionNode? = null, node: CstNode)
      : this(variable, expression, owner, node.tokenStart, node.tokenEnd)
  constructor(variable: Variable, expression: com.tambapps.marcel.semantic.ast.expression.ExpressionNode, owner: com.tambapps.marcel.semantic.ast.expression.ExpressionNode? = null)
      : this(variable, expression, owner, expression.tokenStart, expression.tokenEnd)

  override fun toString() = if (owner != null) "${owner}.$variable = $expression" else "$variable = $expression"
}