package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.type.JavaPrimitiveType
import com.tambapps.marcel.parser.visitor.ExpressionVisitor

class BlockNode(val statements: List<StatementNode>) : ExpressionNode {

  override val type = statements.lastOrNull()?.expressionType ?: JavaPrimitiveType.VOID

  override fun accept(expressionVisitor: ExpressionVisitor) {
    TODO("Not yet implemented")
  }

  override fun toString(): String {
    return "{\n" + statements.joinToString { "\n  $it" } + "\n}"
  }
}