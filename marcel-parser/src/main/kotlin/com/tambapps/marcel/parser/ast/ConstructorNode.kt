package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.owner.NoOpOwner
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Script

class ConstructorNode(
  val superType: JavaType,
  access: Int,
  block: FunctionBlockNode,
  parameters: MutableList<MethodParameter>,
  scope: MethodScope
) : MethodNode(access, JavaType.void, JavaMethod.CONSTRUCTOR_NAME, blockWithSuperCall(scope, block), parameters, JavaType.void,
    scope.apply { addLocalVariable(superType, "super") }, false) {

  override val isConstructor = true
  companion object {
    private fun blockWithSuperCall(scope: Scope, block: FunctionBlockNode): FunctionBlockNode {
      if (block.statements.firstOrNull()?.expression is SuperConstructorCallNode) {
        return block
      }
      val statements = mutableListOf<StatementNode>()
      statements.add(ExpressionStatementNode(SuperConstructorCallNode(scope, mutableListOf())))
      statements.addAll(block.statements)
      return FunctionBlockNode(block.scope, statements)
    }
  }
}