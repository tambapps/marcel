package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode

class TryCatchCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val tryNode: StatementCstNode,
  val resources: List<VariableDeclarationCstNode>,
  // throwable types, throwableVar name, statement
  val catchNodes: List<Triple<List<TypeCstNode>, String, StatementCstNode>>,
  val finallyNode: StatementCstNode?
) :
  AbstractStatementCstNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)
}