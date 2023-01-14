package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor

class VariableReferenceExpression(override val type: JavaType, val name: String): ExpressionNode {

  constructor(name: String, scope: Scope): this(scope.getLocalVariable(name).type, name)

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return name
  }
}