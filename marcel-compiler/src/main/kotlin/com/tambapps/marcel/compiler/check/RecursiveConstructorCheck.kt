package com.tambapps.marcel.compiler.check

import com.tambapps.marcel.compiler.ClassNodeVisitor
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ConstructorNode
import com.tambapps.marcel.parser.ast.expression.ThisConstructorCallNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException

class RecursiveConstructorCheck: ClassNodeVisitor {
  override fun visit(classNode: ClassNode, typeResolver: JavaTypeResolver) {
    classNode.constructors.forEach {
      checkConstructor(classNode, it, typeResolver)
    }
  }

  private fun checkConstructor(classNode: ClassNode, constructorNode: ConstructorNode, typeResolver: JavaTypeResolver) {
    val visitedConstructors = mutableListOf(constructorNode)
    var currentConstructor = constructorNode
    while (true) {
      val firstStatement = currentConstructor.ownConstructorCall ?: break
      if (firstStatement !is ThisConstructorCallNode) break
      val nextConstructor = classNode.constructors.find { it.matches(firstStatement.getMethod(typeResolver)) } ?: break
      if (visitedConstructors.contains(nextConstructor)) {
        throw MarcelSemanticException(constructorNode.token,
          "Constructor $currentConstructor has an infinite constructor call cycle")
      }
      visitedConstructors.add(nextConstructor)
      currentConstructor = nextConstructor
    }
  }
}