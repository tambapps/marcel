package com.tambapps.marcel.semantic.processor.scope

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.processor.imprt.ImportResolver
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.variable.field.MarcelField

/**
 * Scope inside a class
 */
class ClassScope(
  symbolResolver: MarcelSymbolResolver,
  override val classType: JavaType,
  override val forExtensionType: JavaType?,
  importResolver: ImportResolver
) : AbstractScope(symbolResolver, classType.packageName, importResolver) {

  override fun findField(name: String): MarcelField? {
    var type: JavaType? = classType
    while (type != null) {
      val f = symbolResolver.findField(type, name)
      if (f != null) return f
      type = type.outerTypeName?.let { symbolResolver.of(it, emptyList(), LexToken.DUMMY) }
    }

    // find from imports
    val fieldOwner = importResolver.resolveMemberOwnerType(name) ?: return null
    val field = symbolResolver.findField(fieldOwner, name) ?: return null
    return if (field.isMarcelStatic) field else null
  }

  override fun findLocalVariable(name: String) = null

  override fun resolveTypeOrThrow(node: TypeCstNode): JavaType {
    // try to find inner class with this name
    val innerClassName = if (classType.innerName == node.value) classType.className
    else classType.className + '$' + node.value
    if (symbolResolver.isDefined(innerClassName)) return of(innerClassName, node)
    return super.resolveTypeOrThrow(node)
  }
}