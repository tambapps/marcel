package com.tambapps.marcel.parser.cst.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor

class CharCstNode(parent: CstNode? = null, override val value: Char, tokenStart: LexToken, tokenEnd: LexToken)
    : AbstractExpressionCstNode(parent, tokenStart, tokenEnd),
    ExpressionCstNode {
    override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

    override fun toString() = "`${value}`"

}