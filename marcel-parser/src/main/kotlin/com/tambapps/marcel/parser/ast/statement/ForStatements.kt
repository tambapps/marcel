package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.expression.BlockNode
import com.tambapps.marcel.parser.ast.expression.BooleanExpressionNode
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.scope.InnerScope
import com.tambapps.marcel.parser.type.JavaType

abstract class AbstractForStatement(val body: BlockNode): StatementNode, ScopedNode<InnerScope> {

}
class ForStatement(override var scope: InnerScope, val initStatement: StatementNode,
                   val endCondition: BooleanExpressionNode, val iteratorStatement: StatementNode, body: BlockNode): AbstractForStatement(body) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "for ($initStatement $endCondition $iteratorStatement) {\n$body\n}"
  }

}

class ForInStatement constructor(override var scope: InnerScope, val variableType: JavaType, val variableName: String, val inExpression: ExpressionNode, body: BlockNode): AbstractForStatement(body) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "for ($variableType $variableName in $inExpression) {\n$body\n}"
  }

}