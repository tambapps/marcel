package com.tambapps.marcel.semantic.imprt

import com.tambapps.marcel.semantic.type.JavaType

class MutableImportResolver internal constructor(
  typeImports: MutableMap<String, JavaType>,
  wildcardTypeImportPrefixes: MutableSet<String>,
  staticMemberImports: MutableMap<String, JavaType>,
  ): ImportResolver(typeImports, wildcardTypeImportPrefixes, staticMemberImports) {

    companion object {
      fun empty() = MutableImportResolver(LinkedHashMap(), LinkedHashSet(), LinkedHashMap())
    }

    fun isEmpty() = typeImports.isEmpty() && wildcardTypeImportPrefixes.isEmpty() && staticMemberImports.isEmpty()

    fun toImmutable() = ImportResolver(typeImports.toMap(), wildcardTypeImportPrefixes.toSet(), staticMemberImports.toMap())

    fun add(imports: ImportResolver) {
      (typeImports as MutableMap).putAll(imports.typeImports)
      (wildcardTypeImportPrefixes as MutableCollection).addAll(imports.wildcardTypeImportPrefixes)
      (staticMemberImports as MutableMap).putAll(imports.staticMemberImports)
    }
  }
