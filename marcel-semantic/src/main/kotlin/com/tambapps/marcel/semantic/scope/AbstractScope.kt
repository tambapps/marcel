package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.type.JavaTyped

abstract class AbstractScope(
  internal val typeResolver: JavaTypeResolver,
  override val classType: JavaType,
  override val imports: List<ImportNode>,
): Scope {

  override fun findMethod(name: String, argumentTypes: List<JavaTyped>): JavaMethod? {
    return typeResolver.findMethod(classType, name, argumentTypes)
    // fallback on static imported method
      ?: imports.asSequence().mapNotNull { it.resolveMethod(typeResolver, name, argumentTypes) }.firstOrNull()
  }

  override fun resolveTypeOrThrow(node: TypeCstNode): JavaType {
    // try to find inner class with this name
    val innerClassName = if (classType.innerName == node.value) classType.className
    else classType.className + '$' + node.value
    if (typeResolver.isDefined(innerClassName)) return typeResolver.of(innerClassName, node.genericTypes.map { resolveTypeOrThrow(it) }).array(node.arrayDimensions)
    val className = resolveClassName(node.value)
    return typeResolver.of(className, node.genericTypes.map { resolveTypeOrThrow(it) }).array(node.arrayDimensions)
  }

  private fun resolveClassName(classSimpleName: String): String {
    val matchedClasses = imports.mapNotNull { it.resolveClassName(typeResolver, classSimpleName) }.toSet()
    return if (matchedClasses.isEmpty()) classSimpleName
    else if (matchedClasses.size == 1) matchedClasses.first()
    else throw MarcelSemanticException("Ambiguous import for class $classSimpleName")
  }
}