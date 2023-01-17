package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType

/**
 * Node for a super call in a constructor
 */
class SuperConstructorCallNode(scope: Scope, arguments: MutableList<ExpressionNode>) : FunctionCallNode(scope, JavaMethod.CONSTRUCTOR_NAME, arguments) {

  override var type: JavaType
    get() = JavaType.void
    set(value) {
      throw UnsupportedOperationException("Cannot change type of $javaClass")
    }
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }
}