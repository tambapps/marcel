package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.ast.expression.BooleanExpressionNode
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.expression.VoidExpression
import com.tambapps.marcel.parser.type.JavaType

class IfStatementNode(val condition: BooleanExpressionNode, val trueStatementNode: StatementNode,
                      var falseStatementNode: StatementNode?):
  StatementNode {

  override val expression: ExpressionNode
    get() = VoidExpression()
  override val type: JavaType
    get() = trueStatementNode.type

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    condition.accept(visitor)
    trueStatementNode.accept(visitor)
    falseStatementNode?.accept(visitor)
  }
  override fun toString(): String {
    var s = "if ($condition) $trueStatementNode"
    if (falseStatementNode != null) {
      s += " else $falseStatementNode"
    }
    return s
  }
}