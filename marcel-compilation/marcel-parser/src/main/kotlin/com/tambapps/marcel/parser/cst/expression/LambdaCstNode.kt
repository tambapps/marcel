package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.AbstractCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class LambdaCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val parameters: List<MethodParameterCstNode>,
  val blockCstNode: BlockCstNode,
  val explicit0Parameters: Boolean,
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  class MethodParameterCstNode(
    parent: CstNode?,
    tokenStart: LexToken,
    tokenEnd: LexToken,
    val type: TypeCstNode?,
    val name: String,
    val nullable: Boolean,
  ) : AbstractCstNode(parent, tokenStart, tokenEnd) {

    override fun toString(): String {
      return if (type != null) "$type $name" else name
    }

  }
}