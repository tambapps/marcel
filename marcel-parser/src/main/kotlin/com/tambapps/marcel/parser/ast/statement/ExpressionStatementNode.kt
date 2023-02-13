package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.ast.expression.BlockNode

open class ExpressionStatementNode(val expression: ExpressionNode): StatementNode {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    expression.accept(visitor)
  }
  override fun toString(): String {
    return "$expression;"
  }
}

class BlockStatement(val block: BlockNode): ExpressionStatementNode(block) {

}