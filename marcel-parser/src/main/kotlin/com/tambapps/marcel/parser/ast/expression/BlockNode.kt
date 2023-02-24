package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.scope.MethodScope


open class BlockNode constructor(token: LexToken, override var scope: MethodScope, val statements: MutableList<StatementNode>) : AbstractExpressionNode(token), ScopedNode<MethodScope> {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "{\n" + statements.joinToString(transform = { "\n  $it" }) + "\n}"
  }

  fun addStatement(expression: ExpressionNode) {
    addStatement(ExpressionStatementNode(expression.token, expression))
  }

  fun addStatement(statementNode: StatementNode) {
    statements.add(statementNode)
  }
}

// need to differentiate both because we don't always want to push on stack values for "normal" block nodes
class FunctionBlockNode constructor(lexToken: LexToken, scope: MethodScope, statements: MutableList<StatementNode>) : BlockNode(lexToken, scope, statements) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  fun asSimpleBlock(token: LexToken, scope: MethodScope? = null): BlockNode {
    return BlockNode(token, scope ?: this.scope, this.statements)
  }
}