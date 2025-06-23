package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

/**
 * Node for a POP asm instruction.
 * This node is handy for Question Dot operator
 */
// TODO delete this node
class PopNode(val popType: JavaType, cstNode: CstNode) :
  AbstractExpressionNode(JavaType.void, cstNode) {

  override val nullness: Nullness
    get() = Nullness.UNKNOWN

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}