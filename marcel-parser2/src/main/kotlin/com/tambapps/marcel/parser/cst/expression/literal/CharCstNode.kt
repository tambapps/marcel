package com.tambapps.marcel.parser.cst.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.CstExpressionNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor

class CharCstNode(parent: CstNode? = null, override val value: Char, tokenStart: LexToken, tokenEnd: LexToken)
    : AbstractExpressionCstNode(parent, tokenStart, tokenEnd),
    CstExpressionNode {
    override fun <T> accept(visitor: ExpressionCstNodeVisitor<T>) = visitor.visit(this)

    override fun toString() = "`${value}`"

}