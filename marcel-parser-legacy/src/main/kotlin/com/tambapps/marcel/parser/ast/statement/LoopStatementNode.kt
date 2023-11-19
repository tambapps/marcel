package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.InnerScope

class BreakLoopNode(token: LexToken, override var scope: InnerScope): AbstractStatementNode(token), ScopedNode<InnerScope> {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}
class ContinueLoopNode(token: LexToken, override var scope: InnerScope): AbstractStatementNode(token), ScopedNode<InnerScope> {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}