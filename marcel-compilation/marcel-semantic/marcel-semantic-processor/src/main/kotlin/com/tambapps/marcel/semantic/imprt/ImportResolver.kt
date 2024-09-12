package com.tambapps.marcel.semantic.imprt

import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.type.JavaType

/**
 * Class allowing to resolve types from imports
 */
open class ImportResolver internal constructor(
  val typeImports: Map<String, JavaType>, // valueSimpleName -> value, asName -> value
  val wildcardTypeImportPrefixes: Collection<String>,
  val staticMemberImports: Map<String, JavaType>, // memberName -> memberOwnerType
  val extensionTypes: Set<JavaType>
) {

  companion object {
    val DEFAULT_IMPORTS = ImportResolver(
      typeImports = mapOf(),
      wildcardTypeImportPrefixes = listOf(
        "java.lang",
        "java.util",
        "java.io",
        "marcel.lang",
        "marcel.io"
        ),
      staticMemberImports = mapOf(),
      extensionTypes = emptySet()
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

  fun resolveMemberOwnerType(memberName: String): JavaType? {
    return staticMemberImports[memberName]
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

  operator fun plus(other: ImportResolver): ImportResolver {
    return plus(typeImports = other.typeImports, wildcardTypeImportPrefixes = other.wildcardTypeImportPrefixes, staticMemberImports = other.staticMemberImports)
  }

  fun copy(
    typeImports: Map<String, JavaType> = this.typeImports,
    wildcardTypeImportPrefixes: Collection<String> = this.wildcardTypeImportPrefixes,
    staticMemberImports: Map<String, JavaType> = this.staticMemberImports,
    extensionTypes: Set<JavaType> = this.extensionTypes
  ) = ImportResolver(typeImports, wildcardTypeImportPrefixes, staticMemberImports, extensionTypes)

  fun toImports() = MutableImportResolver(typeImports.toMutableMap(), wildcardTypeImportPrefixes.toMutableSet(), staticMemberImports.toMutableMap(), LinkedHashSet(extensionTypes))
}