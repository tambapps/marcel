package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.scope.InnerScope
import com.tambapps.marcel.parser.type.JavaType

// TODO try-with-resource
class TryCatchNode constructor(
  token: LexToken,
  val tryStatementNode: StatementNode,
  val catchNodes: List<CatchBlock>,
  val finallyBlock: FinallyBlock?
): AbstractStatementNode(token) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)



  class CatchBlock(val exceptionTypes: List<JavaType>, val exceptionVarName: String,
                   val scope: InnerScope,
                   val statementNode: StatementNode)
  class FinallyBlock(val scope: InnerScope, val statementNode: StatementNode)
}