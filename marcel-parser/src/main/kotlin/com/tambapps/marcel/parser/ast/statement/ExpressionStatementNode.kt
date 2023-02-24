package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.BlockNode

open class ExpressionStatementNode(token: LexToken, val expression: ExpressionNode): AbstractStatementNode(token) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$expression;"
  }
}

class BlockStatement(val block: BlockNode): ExpressionStatementNode(block.token, block) {

}