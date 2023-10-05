package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.AbstractCstNode
import com.tambapps.marcel.parser.cst.CstInstructionNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.CstNodeVisitor
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor

abstract class AbstractStatementCstNode(parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractCstNode(parent, tokenStart, tokenEnd), StatementCstNode, CstInstructionNode {

  constructor(parent: CstNode?, token: LexToken): this(parent, token, token)

  override fun <T> accept(visitor: ExpressionCstNodeVisitor<T>) = throw UnsupportedOperationException()

  override fun <T> accept(visitor: CstNodeVisitor<T>) = accept(visitor as StatementCstNodeVisitor<T>)

}