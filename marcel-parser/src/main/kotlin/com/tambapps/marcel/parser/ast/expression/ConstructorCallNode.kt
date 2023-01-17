package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType


/**
 * Node for new MyClass()
 */
class ConstructorCallNode(scope: Scope, override var type: JavaType, arguments: MutableList<ExpressionNode>): FunctionCallNode(scope, JavaMethod.CONSTRUCTOR_NAME, arguments) {

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "new $type(" + arguments.joinToString(separator = ",") + ")"
  }
}