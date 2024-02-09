package com.tambapps.marcel.semantic.imprt

import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.type.JavaType

/**
 * Class allowing to resolve types from imports
 */
class ImportResolver internal constructor(
  val typeImports: Map<String, JavaType>, // valueSimpleName -> value, asName -> value
  val wildcardTypeImportPrefixes: Collection<String>,
  val staticMemberImports: Map<String, JavaType>, // memberName -> memberOwnerType
) {

  companion object {
    val DEFAULT_IMPORT_RESOLVER = ImportResolver(
      typeImports = mapOf(),
      wildcardTypeImportPrefixes = listOf(
        "java.lang",
        "java.util",
        "java.io",
        "marcel.lang",
        "marcel.io" // TODO remove me
        ),
      staticMemberImports = mapOf()
    )
  }

  fun resolveType(symbolResolver: MarcelSymbolResolver, classSimpleName: String): JavaType? {
    typeImports[classSimpleName]
    return typeImports[classSimpleName]
      ?: wildcardTypeImportPrefixes.firstNotNullOfOrNull {
        try { symbolResolver.of("${it}.$classSimpleName") }
        catch (e: MarcelSemanticException) { null }
      }
  }

  // TODO memberName use it also for enums and static fields
  fun resolveMemberOwnerType(methodName: String): JavaType? {
    return staticMemberImports[methodName]
  }

  fun plus(
    typeImports: Map<String, JavaType> = emptyMap(),
    wildcardTypeImportPrefixes: Collection<String> = emptySet(),
    staticMemberImports: Map<String, JavaType> = emptyMap()
  ) = copy(
    typeImports = typeImports + this.typeImports,
    wildcardTypeImportPrefixes = wildcardTypeImportPrefixes + this.wildcardTypeImportPrefixes,
    staticMemberImports = staticMemberImports + this.staticMemberImports
  )

  fun copy(
    typeImports: Map<String, JavaType> = this.typeImports,
    wildcardTypeImportPrefixes: Collection<String> = this.wildcardTypeImportPrefixes,
    staticMemberImports: Map<String, JavaType> = this.staticMemberImports
  ) = ImportResolver(typeImports, wildcardTypeImportPrefixes, staticMemberImports)
}