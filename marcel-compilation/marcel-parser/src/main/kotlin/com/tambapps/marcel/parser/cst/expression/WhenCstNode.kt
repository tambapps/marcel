package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

open class WhenCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val branches: MutableList<Pair<ExpressionCstNode, StatementCstNode>>,
  val elseStatement: StatementCstNode?
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {
  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun toString() = buildString {
    appendHeader(this)
    branches.forEach { (condition, statement) ->
      append("  $condition -> $statement\n")
    }
    if (elseStatement != null) {
      append("  else -> $elseStatement\n")
    }
    append("}")
  }

  open protected fun appendHeader(builder: StringBuilder) {
    builder.append("when {\n")
  }
}