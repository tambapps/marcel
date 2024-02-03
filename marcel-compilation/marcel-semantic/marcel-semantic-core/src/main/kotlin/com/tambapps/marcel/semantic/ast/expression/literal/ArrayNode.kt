package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.type.JavaArrayType
import com.tambapps.marcel.semantic.type.JavaType

class ArrayNode constructor(
  val elements: MutableList<com.tambapps.marcel.semantic.ast.expression.ExpressionNode>,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  var _type: JavaArrayType? = null
  // don't care about type passed to parent. we will override it anyway
) : com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(JavaType.Object, tokenStart, tokenEnd) {

  constructor(
    elements: MutableList<com.tambapps.marcel.semantic.ast.expression.ExpressionNode>,
    node: CstNode,
    _type: JavaArrayType? = null
  ): this(elements, node.tokenStart, node.tokenEnd, _type)

  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

  override val type: JavaArrayType
    get() {
      return if (_type == null && elements.isEmpty()) JavaType.Object.arrayType
      else _type ?: JavaType.commonType(elements).arrayType
    }

}