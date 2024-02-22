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

  data class Imports internal constructor(
    val typeImports: MutableMap<String, JavaType>,
    val wildcardTypeImportPrefixes: MutableSet<String>,
    val staticMemberImports: MutableMap<String, JavaType>,
  ) {
    companion object {
      fun empty() = Imports(LinkedHashMap(), LinkedHashSet(), LinkedHashMap())
    }

    fun isEmpty() = typeImports.isEmpty() && wildcardTypeImportPrefixes.isEmpty() && staticMemberImports.isEmpty()

    fun toResolver() = ImportResolver(typeImports.toMap(), wildcardTypeImportPrefixes.toSet(), staticMemberImports.toMap())

    fun add(imports: Imports) {
      typeImports.putAll(imports.typeImports)
      wildcardTypeImportPrefixes.addAll(imports.wildcardTypeImportPrefixes)
      staticMemberImports.putAll(imports.staticMemberImports)
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

  fun copy(
    typeImports: Map<String, JavaType> = this.typeImports,
    wildcardTypeImportPrefixes: Collection<String> = this.wildcardTypeImportPrefixes,
    staticMemberImports: Map<String, JavaType> = this.staticMemberImports
  ) = ImportResolver(typeImports, wildcardTypeImportPrefixes, staticMemberImports)

  fun toImports() = Imports(typeImports.toMutableMap(), wildcardTypeImportPrefixes.toMutableSet(), staticMemberImports.toMutableMap())
}