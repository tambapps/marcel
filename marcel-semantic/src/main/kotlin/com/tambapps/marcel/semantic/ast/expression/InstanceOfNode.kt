package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.type.JavaType

class InstanceOfNode(val instanceType: JavaType,
                     val expressionNode: ExpressionNode,
                     node: CstNode
) : AbstractExpressionNode(JavaType.boolean, node) {
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)
}