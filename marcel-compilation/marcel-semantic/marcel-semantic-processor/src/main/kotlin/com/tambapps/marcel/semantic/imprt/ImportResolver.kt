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
) {

  // TODO rename MutableImportResolver and make it a top-level class
  class Imports internal constructor(
    typeImports: MutableMap<String, JavaType>,
    wildcardTypeImportPrefixes: MutableSet<String>,
    staticMemberImports: MutableMap<String, JavaType>,
  ): ImportResolver(typeImports, wildcardTypeImportPrefixes, staticMemberImports) {
    companion object {
      fun empty() = Imports(LinkedHashMap(), LinkedHashSet(), LinkedHashMap())
    }

    fun isEmpty() = typeImports.isEmpty() && wildcardTypeImportPrefixes.isEmpty() && staticMemberImports.isEmpty()

    fun toResolver() = ImportResolver(typeImports.toMap(), wildcardTypeImportPrefixes.toSet(), staticMemberImports.toMap())

    fun add(imports: Imports) {
      (typeImports as MutableMap).putAll(imports.typeImports)
      (wildcardTypeImportPrefixes as MutableCollection).addAll(imports.wildcardTypeImportPrefixes)
      (staticMemberImports as MutableMap).putAll(imports.staticMemberImports)
    }
  }

  companion object {
    val DEFAULT_IMPORTS = ImportResolver(
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
    staticMemberImports: Map<String, JavaType> = this.staticMemberImports
  ) = ImportResolver(typeImports, wildcardTypeImportPrefixes, staticMemberImports)

  fun toImports() = Imports(typeImports.toMutableMap(), wildcardTypeImportPrefixes.toMutableSet(), staticMemberImports.toMutableMap())
}