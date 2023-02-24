package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.type.JavaType

open class TernaryNode(token: LexToken, val boolExpression: BooleanExpressionNode,
                       val trueExpression: ExpressionNode,
                       val falseExpression: ExpressionNode
): AbstractExpressionNode(token) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$boolExpression ? $trueExpression : $falseExpression"
  }
}