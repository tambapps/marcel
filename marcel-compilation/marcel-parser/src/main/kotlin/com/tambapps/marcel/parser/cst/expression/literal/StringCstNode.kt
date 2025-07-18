package com.tambapps.marcel.parser.cst.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class StringCstNode(parent: CstNode? = null, override val value: String, tokenStart: LexToken, tokenEnd: LexToken)
    : AbstractExpressionCstNode(parent, tokenStart, tokenEnd),
    ExpressionCstNode {

    constructor(parent: CstNode? = null, value: String, token: LexToken): this(parent, value, token, token)

    override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

    override fun toString() = "\"$value\""

}