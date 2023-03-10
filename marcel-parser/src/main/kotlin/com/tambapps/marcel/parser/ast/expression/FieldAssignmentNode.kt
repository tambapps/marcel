package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.Scope

class FieldAssignmentNode constructor(token: LexToken,
                                      override var scope: Scope, val fieldNode: GetFieldAccessOperator,
                                      val expression: ExpressionNode): AbstractExpressionNode(token), ScopedNode<Scope> {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "$fieldNode = $expression"
  }
}