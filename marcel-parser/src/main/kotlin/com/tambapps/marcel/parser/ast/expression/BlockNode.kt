package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.type.JavaPrimitiveType
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.visitor.ExpressionVisitor

open class BlockNode(val statements: List<StatementNode>) : ExpressionNode {

  override val type = statements.lastOrNull()?.type ?: JavaPrimitiveType.VOID

  override fun accept(expressionVisitor: ExpressionVisitor) {
    expressionVisitor.visit(this)
  }

  override fun toString(): String {
    return "{\n" + statements.joinToString { "\n  $it" } + "\n}"
  }

  fun toFunctionBlock(methodReturnType: JavaType): FunctionBlockNode {
    return FunctionBlockNode(methodReturnType, statements)
  }
}

// need to differentiate both because we don't always want to push on stack values for "normal" block nodes
class FunctionBlockNode(val methodReturnType: JavaType, statements: List<StatementNode>) : BlockNode(statements) {
  override fun accept(expressionVisitor: ExpressionVisitor) {
    expressionVisitor.visit(this)
  }
}