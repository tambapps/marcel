package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType

class TryCatchNode(
  token: LexToken,
  val tryStatementNode: StatementNode,
  val catchNodes: List<CatchBlock>,
  val finallyBlock: StatementNode? // TODO handle me
): AbstractStatementNode(token) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)



  class CatchBlock(val exceptionTypes: List<JavaType>, val exceptionVarName: String, val statementNode: StatementNode)
}