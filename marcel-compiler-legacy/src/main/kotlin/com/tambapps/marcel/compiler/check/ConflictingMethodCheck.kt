package com.tambapps.marcel.compiler.check

import com.tambapps.marcel.compiler.ClassNodeVisitor
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.exception.MarcelSemanticLegacyException

/**
 * now check for conflicting methods
 */
internal class ConflictingMethodCheck: ClassNodeVisitor {

  override fun visit(classNode: ClassNode, typeResolver: JavaTypeResolver) {
    for (methodNode in classNode.methods) {
      // also define method parameters. Couldn't do it in parser because it would mean parse the type
      methodNode.scope.defineParametersInScope()
      val conflictMethod = classNode.methods.find { methodNode !== it && it.matches(methodNode) }
      if (conflictMethod != null) throw MarcelSemanticLegacyException(
        conflictMethod.token,
        "Method $methodNode conflicts with $conflictMethod"
      )
    }
  }
}