package com.tambapps.marcel.parser.cst.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor

class StringCstNode(parent: CstNode? = null, override val value: String, tokenStart: LexToken, tokenEnd: LexToken)
    : AbstractExpressionCstNode(parent, tokenStart, tokenEnd),
    ExpressionCstNode {
    override fun <T> accept(visitor: ExpressionCstNodeVisitor<T>) = visitor.visit(this)

    override fun toString() = "\"$value\""

}