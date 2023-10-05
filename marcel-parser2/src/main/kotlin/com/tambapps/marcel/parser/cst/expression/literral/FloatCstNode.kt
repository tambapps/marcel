package com.tambapps.marcel.parser.cst.expression.literral

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.CstExpressionNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor

class FloatCstNode(parent: CstNode? = null, override val value: Float, token: LexToken) : AbstractExpressionCstNode(parent, token),
    CstExpressionNode {
    override fun <T> accept(visitor: ExpressionCstNodeVisitor<T>) = visitor.visit(this)

    override fun toString() = "${value}f"

}