package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Opcodes

class ConstructorNode(
  token: LexToken,
  access: Int,
  block: FunctionBlockNode,
  parameters: MutableList<MethodParameterNode>,
  scope: MethodScope
) : MethodNode(token, access, JavaType.void, JavaMethod.CONSTRUCTOR_NAME, blockWithSuperCall(scope, block), parameters, JavaType.void,
  scope, false, true) {

  companion object {

    fun of(classNode: ClassNode, parameters: MutableList<MethodParameterNode>, statements: MutableList<StatementNode>): ConstructorNode {
      return of(classNode, MethodScope(classNode.scope, JavaMethod.CONSTRUCTOR_NAME, parameters, JavaType.void), parameters, statements)
    }
    fun of(classNode: ClassNode, scope: MethodScope, parameters: MutableList<MethodParameterNode>, statements: MutableList<StatementNode>): ConstructorNode {
      return ConstructorNode(classNode.token,
        Opcodes.ACC_PUBLIC, FunctionBlockNode(LexToken.dummy(), scope, statements),
        parameters, scope
      )
    }

    fun emptyConstructor(classNode: ClassNode): ConstructorNode {
      val emptyConstructorScope = MethodScope(classNode.scope, JavaMethod.CONSTRUCTOR_NAME, emptyList(), JavaType.void)
      return ConstructorNode(classNode.token,
        Opcodes.ACC_PUBLIC, FunctionBlockNode(classNode.token, emptyConstructorScope, mutableListOf()),
        mutableListOf(), emptyConstructorScope
      )
    }

    // add super call if it isn't there
    private fun blockWithSuperCall(scope: Scope, block: FunctionBlockNode): FunctionBlockNode {
      val firstStatement = block.statements.firstOrNull()
      if (firstStatement is ExpressionStatementNode && firstStatement.expression is SuperConstructorCallNode) {
        return block
      }

      val statements = mutableListOf<StatementNode>()
      statements.add(ExpressionStatementNode(block.token, SuperConstructorCallNode(block.token, scope, mutableListOf())))
      statements.addAll(block.statements)
      return FunctionBlockNode(block.token, block.scope, statements)
    }
  }
}