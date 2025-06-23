package com.tambapps.marcel.semantic.processor.check

import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ThisConstructorCallNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.processor.visitor.ClassNodeVisitor

/**
 * Check that defined constructors don't recursively call each others
 */
internal class RecursiveConstructorCheck : ClassNodeVisitor {

  override fun visit(classNode: ClassNode, symbolResolver: MarcelSymbolResolver) {
    classNode.constructors.forEach {
      checkConstructor(classNode, it)
    }
  }

  private fun checkConstructor(classNode: ClassNode, constructorNode: MethodNode) {
    val visitedConstructors = mutableListOf(constructorNode)
    var currentConstructor = constructorNode
    while (true) {
      val firstStatement =
        (currentConstructor.blockStatement.statements.firstOrNull() as? ExpressionStatementNode)?.expressionNode ?: break
      if (firstStatement !is ThisConstructorCallNode) break
      val nextConstructor = classNode.constructors.find { it.matches(firstStatement.javaMethod) } ?: break
      if (visitedConstructors.contains(nextConstructor)) {
        throw MarcelSemanticException(
          constructorNode.token,
          "Constructor $constructorNode has an infinite constructor call cycle"
        )
      }
      visitedConstructors.add(nextConstructor)
      currentConstructor = nextConstructor
    }
  }
}