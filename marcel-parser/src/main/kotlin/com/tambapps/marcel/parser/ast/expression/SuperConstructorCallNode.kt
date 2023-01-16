package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType

/**
 * Node for a super call in a constructor
 */
class SuperConstructorCallNode(arguments: MutableList<ExpressionNode>) : FunctionCallNode("<init>", arguments) {

  override val type = JavaType.void
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }
}