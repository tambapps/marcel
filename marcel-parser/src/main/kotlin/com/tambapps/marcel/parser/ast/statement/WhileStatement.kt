package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.expression.BlockNode
import com.tambapps.marcel.parser.ast.expression.BooleanExpressionNode
import com.tambapps.marcel.parser.scope.InnerScope

class WhileStatement constructor(token: LexToken, override var scope: InnerScope, val condition: BooleanExpressionNode, val body: BlockNode): AbstractStatementNode(token),
  ScopedNode<InnerScope> {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}