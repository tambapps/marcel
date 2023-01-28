package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.expression.VoidExpression
import com.tambapps.marcel.parser.scope.InnerScope

class BreakLoopNode(override var scope: InnerScope): StatementNode, ScopedNode<InnerScope> {

  override val expression = VoidExpression()
  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }
}
class ContinueLoopNode(val scope: InnerScope): StatementNode {

  override val expression = VoidExpression()
  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }
}