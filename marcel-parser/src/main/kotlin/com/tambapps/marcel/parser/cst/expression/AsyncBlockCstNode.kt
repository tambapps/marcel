package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.statement.BlockCstNode

class AsyncBlockCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val block: BlockCstNode
) :
  AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AsyncBlockCstNode) return false
    if (!super.equals(other)) return false

    if (block != other.block) return false

    return true
  }

  override fun hashCode(): Int {
    return block.hashCode()
  }

  override fun toString(): String {
    return "async $block"
  }
}