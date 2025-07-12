package com.tambapps.marcel.parser.cst.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class LongCstNode(parent: CstNode? = null, override val value: Long, token: LexToken) : AbstractExpressionCstNode(parent, token),
    ExpressionCstNode {
    override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

    override fun toString() = "${value}L"

}