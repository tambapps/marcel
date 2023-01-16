package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType

open class FunctionCallNode(override val type: JavaType, val name: String, val arguments: MutableList<ExpressionNode>): ExpressionNode {


  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return name + "(" + arguments.joinToString(separator = ",") + ") " + type
  }
}