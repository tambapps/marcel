package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.visitor.StatementVisitor

class ReturnNode(val expressionNode: ExpressionNode) : StatementNode {
  override val expressionType: JavaType
    get() = expressionNode.type

  override fun accept(mv: StatementVisitor) {
    TODO("Not yet implemented")
  }
}