package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.BlockNode
import com.tambapps.marcel.parser.ast.expression.BooleanExpressionNode
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.expression.VoidExpression
import com.tambapps.marcel.parser.type.JavaType

class ForStatement(val initStatement: StatementNode,
                   val endCondition: BooleanExpressionNode, val iteratorStatement: StatementNode, val body: StatementNode): StatementNode {

  override val expression = VoidExpression()


  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }

  override fun toString(): String {
    return "for ($initStatement $endCondition $iteratorStatement) {\n$body\n}"
  }
}

class ForInStatement(val variableType: JavaType, val variableName: String, val inExpression: ExpressionNode, val body: BlockNode): StatementNode {
  override val expression = VoidExpression()
  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }

  override fun toString(): String {
    return "for ($variableType $variableName in $inExpression) {\n$body\n}"
  }
}