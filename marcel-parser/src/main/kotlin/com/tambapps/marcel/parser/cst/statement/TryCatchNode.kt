package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeNode

class TryCatchNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val tryNode: StatementNode,
  val resources: List<VariableDeclarationNode>,
  // throwable types, throwableVar name, statement
  val catchNodes: List<Triple<List<TypeNode>, String, StatementNode>>,
  val finallyNode: StatementNode?
) :
  AbstractStatementNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)
}