package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.AbstractCstNode
import com.tambapps.marcel.parser.cst.CstInstructionNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.CstNodeVisitor
import com.tambapps.marcel.parser.cst.statement.StatementCstNodeVisitor

abstract class AbstractExpressionCstNode(parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractCstNode(parent, tokenStart, tokenEnd), CstExpressionNode, CstInstructionNode {

    constructor(parent: CstNode?, token: LexToken): this(parent, token, token)

  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = throw UnsupportedOperationException()

  override fun <T> accept(visitor: CstNodeVisitor<T>) = accept(visitor as ExpressionCstNodeVisitor<T>)

}