package com.tambapps.marcel.semantic.processor.imprt

import com.tambapps.marcel.semantic.type.JavaType

class MutableImportResolver internal constructor(
  typeImports: MutableMap<String, JavaType>,
  wildcardTypeImportPrefixes: MutableSet<String>,
  staticMemberImports: MutableMap<String, JavaType>,
  extensionTypes: MutableSet<JavaType>
  ): ImportResolver(typeImports, wildcardTypeImportPrefixes, staticMemberImports, extensionTypes) {

    companion object {
      fun empty() = MutableImportResolver(LinkedHashMap(), LinkedHashSet(), LinkedHashMap(), LinkedHashSet())
    }

    fun isEmpty() = typeImports.isEmpty() && wildcardTypeImportPrefixes.isEmpty() && staticMemberImports.isEmpty()

    fun toImmutable() = ImportResolver(typeImports.toMap(), wildcardTypeImportPrefixes.toSet(), staticMemberImports.toMap(), LinkedHashSet(extensionTypes))

    fun add(imports: ImportResolver) {
      (typeImports as MutableMap).putAll(imports.typeImports)
      (wildcardTypeImportPrefixes as MutableCollection).addAll(imports.wildcardTypeImportPrefixes)
      (staticMemberImports as MutableMap).putAll(imports.staticMemberImports)
      (extensionTypes as MutableSet).addAll(imports.extensionTypes)
    }
  }
