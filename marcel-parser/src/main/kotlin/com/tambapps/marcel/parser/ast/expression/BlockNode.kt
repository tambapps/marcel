package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.scope.MethodScope


open class BlockNode constructor(override var scope: MethodScope, val statements: MutableList<StatementNode>) : ExpressionNode, ScopedNode<MethodScope> {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "{\n" + statements.joinToString(transform = { "\n  $it" }) + "\n}"
  }

  fun addStatement(expression: ExpressionNode) {
    addStatement(ExpressionStatementNode(expression))
  }

  fun addStatement(statementNode: StatementNode) {
    statements.add(statementNode)
  }
  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    statements.forEach { it.accept(visitor) }
  }
}

// need to differentiate both because we don't always want to push on stack values for "normal" block nodes
class FunctionBlockNode constructor(scope: MethodScope, statements: MutableList<StatementNode>) : BlockNode(scope, statements) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  fun asSimpleBlock(scope: MethodScope? = null): BlockNode {
    return BlockNode(scope ?: this.scope, this.statements)
  }
}