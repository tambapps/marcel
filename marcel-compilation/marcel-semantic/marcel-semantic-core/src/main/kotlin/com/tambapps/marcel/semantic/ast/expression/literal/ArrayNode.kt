package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaArrayType

class ArrayNode constructor(
  val elements: MutableList<ExpressionNode>,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  override val type: JavaArrayType // overriding because we know for sure it's an array type
) : AbstractExpressionNode(type, tokenStart, tokenEnd) {

  constructor(
    elements: MutableList<ExpressionNode>,
    node: CstNode,
    type: JavaArrayType
  ) : this(elements, node.tokenStart, node.tokenEnd, type)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)
}