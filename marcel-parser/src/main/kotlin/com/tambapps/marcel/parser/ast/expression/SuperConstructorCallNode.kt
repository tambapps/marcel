package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor

/**
 * Node for a super call in a constructor
 */
class SuperConstructorCallNode(arguments: MutableList<ExpressionNode>) : FunctionCallNode("<init>", arguments) {

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }
}