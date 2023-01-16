package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType


/**
 * Node for new MyClass()
 */
class ConstructorCallNode(type: JavaType, arguments: MutableList<ExpressionNode>): FunctionCallNode(type, "<init>", arguments) {

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }
}