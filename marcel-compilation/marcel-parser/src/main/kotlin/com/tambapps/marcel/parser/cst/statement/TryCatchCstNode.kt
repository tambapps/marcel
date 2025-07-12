package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is TryCatchCstNode) return false

    if (tryNode != other.tryNode) return false
    if (resources != other.resources) return false
    if (catchNodes != other.catchNodes) return false
    if (finallyNode != other.finallyNode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = tryNode.hashCode()
    result = 31 * result + resources.hashCode()
    result = 31 * result + catchNodes.hashCode()
    result = 31 * result + (finallyNode?.hashCode() ?: 0)
    return result
  }
}