package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.owner.NoOpOwner
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Script

class ConstructorNode(
  val superType: JavaType,
  access: Int,
  block: FunctionBlockNode,
  parameters: MutableList<MethodParameter>,
  scope: Scope
) : MethodNode(access, NoOpOwner(), "<init>", blockWithSuperCall(block), parameters, JavaType.void,
    // TODO don't know if it's the right way, don't know if we should also add it in parameters
    scope.apply { addLocalVariable(superType, "super") }) {

  companion object {
    private fun blockWithSuperCall(block: FunctionBlockNode): FunctionBlockNode {
      if (block.statements.firstOrNull()?.expression is SuperConstructorCallNode) {
        return block
      }
      val statements = mutableListOf<StatementNode>()
      statements.add(ExpressionStatementNode(SuperConstructorCallNode(mutableListOf())))
      statements.addAll(block.statements)
      return FunctionBlockNode(block.methodReturnType, statements)
    }
  }
}