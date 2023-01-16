package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType


/**
 * Node for new MyClass()
 */
class ConstructorCallNode(override var type: JavaType, arguments: MutableList<ExpressionNode>): FunctionCallNode("<init>", arguments) {

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "new $type(" + arguments.joinToString(separator = ",") + ")"
  }
}