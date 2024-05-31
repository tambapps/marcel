package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.exception.MemberNotVisibleException
import com.tambapps.marcel.semantic.imprt.ImportResolver
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver

abstract class AbstractScope(
  internal val symbolResolver: MarcelSymbolResolver,
  private val packageName: String?,
  val importResolver: ImportResolver,
) : Scope {

  override fun resolveTypeOrThrow(node: TypeCstNode): JavaType {
    // search on imports
    importResolver.resolveType(symbolResolver, node.value)?.let { resolvedType ->
      return resolvedType.withGenericTypes(node.genericTypes.map { resolveTypeOrThrow(it) }).array(node.arrayDimensions)
    }

    // search on own package
    val classSimpleName = node.value
    if (packageName != null) {
      val classFullName = "$packageName.$classSimpleName"
      if (symbolResolver.isDefined(classFullName)) return of(classFullName, node).array(node.arrayDimensions)
    }
    val type = of(classSimpleName, node)
    return of(classSimpleName, node).apply {
      if (!type.isVisibleFrom(classType)) {
        throw MemberNotVisibleException(node, type, classType)
      }
    }
  }

  protected fun of(simpleName: String, node: TypeCstNode): JavaType {
    return symbolResolver.of(simpleName, node.genericTypes.map { resolveTypeOrThrow(it) }, node.token)
      .array(node.arrayDimensions)
  }

}