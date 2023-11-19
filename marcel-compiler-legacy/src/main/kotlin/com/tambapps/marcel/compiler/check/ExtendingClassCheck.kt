package com.tambapps.marcel.compiler.check

import com.tambapps.marcel.compiler.ClassNodeVisitor
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException

/**
 * Check that the extended class is accessible and not final
 */
class ExtendingClassCheck: ClassNodeVisitor {
  override fun visit(classNode: ClassNode, typeResolver: JavaTypeResolver) {
    val superType = classNode.superType
    if (superType.isInterface) {
      throw MarcelSemanticException(classNode.token, "Cannot extend a interface $superType. Implement it")
    }
    if (superType.isFinal) {
      throw MarcelSemanticException(classNode.token, "Cannot extend final class $superType")
    }
    if (!superType.isAccessibleFrom(classNode.type)) {
      throw MarcelSemanticException(classNode.token, "Class $superType isn't accessible from package ${classNode.type.packageName ?: ""}")
    }
  }
}