package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor

class LiteralMapNode(val entries: List<Pair<ExpressionNode, ExpressionNode>>): ExpressionNode {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    entries.forEach {
      it.first.accept(visitor)
      it.second.accept(visitor)
    }
  }
}