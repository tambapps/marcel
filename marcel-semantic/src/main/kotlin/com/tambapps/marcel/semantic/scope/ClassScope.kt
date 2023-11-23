package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver

/**
 * Scope inside a class
 */
class ClassScope constructor(
  typeResolver: JavaTypeResolver,
  override val classType: JavaType,
  override val forExtensionType: JavaType?,
  imports: List<ImportNode>
): AbstractScope(typeResolver, classType.packageName, imports) {

  override fun findField(name: String) = typeResolver.findField(classType, name)

  override fun findLocalVariable(name: String) = null

  override fun resolveTypeOrThrow(node: TypeCstNode): JavaType {
    // try to find inner class with this name
    val innerClassName = if (classType.innerName == node.value) classType.className
    else classType.className + '$' + node.value
    if (typeResolver.isDefined(innerClassName)) return of(innerClassName, node)
    return super.resolveTypeOrThrow(node)
  }
}