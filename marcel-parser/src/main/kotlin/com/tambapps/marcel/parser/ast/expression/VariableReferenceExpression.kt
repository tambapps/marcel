package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ResolvableNode

class VariableReferenceExpression(val name: String): ExpressionNode, ResolvableNode {
  override lateinit var type: JavaType

  constructor(type: JavaType, name: String): this(name) {
    this.type = type
  }

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun resolve(scope: Scope) {
    type = scope.getLocalVariable(name).type
  }
  override fun toString(): String {
    return name
  }
}