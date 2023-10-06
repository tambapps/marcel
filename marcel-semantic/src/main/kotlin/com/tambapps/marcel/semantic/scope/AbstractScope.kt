package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.type.JavaTyped

abstract class AbstractScope(
  internal val typeResolver: JavaTypeResolver,
  override val classType: JavaType,
  internal val imports: List<ImportNode>,
): Scope {

  override fun findMethod(name: String, argumentTypes: List<JavaTyped>): JavaMethod? {
    return typeResolver.findMethod(classType, name, argumentTypes)
    // fallback on static imported method
      ?: imports.asSequence().mapNotNull { it.resolveMethod(typeResolver, name, argumentTypes) }.firstOrNull()
  }

}