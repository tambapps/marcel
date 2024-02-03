package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver

abstract class AbstractScope(
  internal val symbolResolver: MarcelSymbolResolver,
  private val packageName: String?,
  val imports: List<ImportNode>,
) : Scope {

  override fun resolveTypeOrThrow(node: TypeCstNode): JavaType {
    // search on imports
    val importClassName = resolveImportClassName(node)
    if (importClassName != null) {
      return of(importClassName, node)
    }

    // search on own package
    val classSimpleName = node.value
    if (packageName != null) {
      val classFullName = "$packageName.$classSimpleName"
      if (symbolResolver.isDefined(classFullName)) return of(classFullName, node).array(node.arrayDimensions)
    }
    return of(classSimpleName, node)
  }

  protected fun of(simpleName: String, node: TypeCstNode): JavaType {
    return symbolResolver.of(simpleName, node.genericTypes.map { resolveTypeOrThrow(it) }, node.token)
      .array(node.arrayDimensions)
  }

  private fun resolveImportClassName(node: TypeCstNode): String? {
    val classSimpleName = node.value
    val matchedClasses = imports.mapNotNull { it.resolveClassName(node.token, symbolResolver, classSimpleName) }.toSet()
    return if (matchedClasses.isEmpty()) null
    else if (matchedClasses.size == 1) matchedClasses.first()
    else throw MarcelSemanticException(node.token, "Ambiguous import for class $classSimpleName")
  }
}