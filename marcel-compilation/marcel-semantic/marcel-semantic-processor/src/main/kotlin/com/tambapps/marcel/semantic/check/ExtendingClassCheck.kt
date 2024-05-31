package com.tambapps.marcel.semantic.check

import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.exception.MemberNotVisibleException
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.visitor.ClassNodeVisitor

/**
 * Check that the extended class is accessible and not final
 */
internal object ExtendingClassCheck : ClassNodeVisitor {

  override fun visit(classNode: ClassNode, symbolResolver: MarcelSymbolResolver) {
    val superType = classNode.superType
    if (superType.isInterface) {
      throw MarcelSemanticException(classNode.token, "Cannot extend a interface $superType. Implement it")
    }
    if (superType.isFinal) {
      throw MarcelSemanticException(classNode.token, "Cannot extend final class $superType")
    }
    if (!superType.isVisibleFrom(classNode.type)) {
      throw MemberNotVisibleException(classNode.token, superType, classNode.type)
    }
  }
}