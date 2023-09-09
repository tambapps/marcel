package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.InnerScope
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType

class TryCatchNode constructor(
  token: LexToken,
  override var scope: MethodScope,
  val resources: List<VariableDeclarationNode>,
  val tryBlock: TryBlock,
  val catchNodes: List<CatchBlock>,
  val finallyBlock: FinallyBlock?
): AbstractStatementNode(token), ScopedNode<MethodScope> {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun trySetScope(scope: Scope) {
    super.trySetScope(scope as? MethodScope ?: throw RuntimeException("Compiler error"))
    tryBlock.scope = InnerScope(scope)
    catchNodes.forEach { it.scope = InnerScope(scope) }
    finallyBlock?.scope = InnerScope(scope)
  }

  class CatchBlock(val exceptionTypes: List<JavaType>, val exceptionVarName: String,
                   var scope: InnerScope,
                   val statementNode: StatementNode)
  class FinallyBlock(var scope: InnerScope, val statementNode: StatementNode)
  class TryBlock(var scope: InnerScope, val statementNode: StatementNode)
}