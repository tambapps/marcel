package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.type.JavaType

class IfStatementNode(val condition: ExpressionNode, val statementNode: StatementNode):
  StatementNode {

  override val expression: ExpressionNode
    get() = throw RuntimeException("Compiler design problem")
  override val type: JavaType
    get() = statementNode.type

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

}