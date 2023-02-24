package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.type.JavaArrayType

// var type useful for emptyArrayNode
open class LiteralArrayNode constructor(token: LexToken, var type: JavaArrayType?, val elements: List<ExpressionNode>): AbstractExpressionNode(token) {

  constructor(token: LexToken, elements: List<ExpressionNode>): this(token, null, elements)
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    val s = elements.joinToString(separator = ", ", prefix = "[", postfix = "]")
    return if (type != null) "$s as $type" else s
  }
}

class EmptyArrayNode(token: LexToken, type: JavaArrayType): LiteralArrayNode(token, type, emptyList())