package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType

class StringNode(val parts: List<ExpressionNode>, node: CstNode) :
  AbstractExpressionNode(JavaType.String, node) {
  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)
}