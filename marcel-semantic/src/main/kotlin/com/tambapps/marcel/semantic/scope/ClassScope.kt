package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.variable.field.MarcelField

/**
 * Scope inside a class
 */
class ClassScope constructor(
  symbolResolver: MarcelSymbolResolver,
  override val classType: JavaType,
  override val forExtensionType: JavaType?,
  imports: List<ImportNode>
): AbstractScope(symbolResolver, classType.packageName, imports) {

  override fun findField(name: String): MarcelField? {
    var type: JavaType? = classType
    while (type != null) {
      val f = symbolResolver.findField(type, name)
      if (f != null) return f
      type = type.outerTypeName?.let { symbolResolver.of(LexToken.DUMMY, it, emptyList()) }
    }
    return null
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