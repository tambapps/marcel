package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ResolvableNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType

open class FunctionCallNode(val name: String, val arguments: MutableList<ExpressionNode>): ExpressionNode, ResolvableNode {

  override lateinit var type: JavaType
  constructor(name: String): this(name, mutableListOf())

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun resolve(scope: Scope) {
    if (name != "println") { // TODO BIG HACK
      this.type = scope.getMethod(name, arguments).returnType
    } else {
      this.type = JavaType.void
    }
  }

  override fun toString(): String {
    return name + "(" + arguments.joinToString(separator = ",") + ")"
  }
}