package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.ast.expression.BooleanExpressionNode
import com.tambapps.marcel.parser.ast.expression.ExpressionNode

class IfStatementNode(token: LexToken, val condition: BooleanExpressionNode, val trueStatementNode: StatementNode,
                      var falseStatementNode: StatementNode?): AbstractStatementNode(token) {

  constructor(condition: ExpressionNode, trueStatementNode: StatementNode,
              falseStatementNode: StatementNode?): this(condition.token, BooleanExpressionNode(condition.token, condition), trueStatementNode, falseStatementNode)

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    var s = "if ($condition) $trueStatementNode"
    if (falseStatementNode != null) {
      s += " else $falseStatementNode"
    }
    return s
  }
}