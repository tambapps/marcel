package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType

open class FunctionCallNode(val name: String, val arguments: MutableList<ExpressionNode>): ExpressionNode {
  // for now only ints are handled
  override val type = JavaType.int

  constructor(name: String): this(name, mutableListOf())

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return name + "(" + arguments.joinToString(separator = ",") + ")"
  }
}