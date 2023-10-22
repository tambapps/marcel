package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.variable.LocalVariable

/**
 * Scope inside a class
 */
class ClassScope(
  classType: JavaType,
  typeResolver: JavaTypeResolver, imports: List<ImportNode>
): AbstractScope(typeResolver, classType, imports) {


  override fun findField(name: String) = typeResolver.findField(classType, name)

  override fun findLocalVariable(name: String) = null
}