package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.type.JavaType

/**
 * Node for a POP asm instruction.
 * This node is handy for Question Dot operator
 */
class PopNode(val popType: JavaType, cstNode: CstNode) : AbstractExpressionNode(JavaType.void, cstNode) {

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

}