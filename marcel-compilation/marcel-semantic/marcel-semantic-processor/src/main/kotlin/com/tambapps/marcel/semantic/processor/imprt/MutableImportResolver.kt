package com.tambapps.marcel.semantic.processor.imprt

import com.tambapps.marcel.semantic.type.JavaType

class MutableImportResolver internal constructor(
  override val typeImports: MutableMap<String, JavaType>,
  override val wildcardTypeImportPrefixes: MutableSet<String>,
  override val staticMemberImports: MutableMap<String, JavaType>,
  override val extensionTypes: MutableSet<JavaType>
  ): ImportResolver(typeImports, wildcardTypeImportPrefixes, staticMemberImports, extensionTypes) {

    companion object {
      fun empty() = MutableImportResolver(LinkedHashMap(), LinkedHashSet(), LinkedHashMap(), LinkedHashSet())
    }

    fun isEmpty() = typeImports.isEmpty() && wildcardTypeImportPrefixes.isEmpty() && staticMemberImports.isEmpty()

    fun toImmutable() = ImportResolver(typeImports.toMap(), wildcardTypeImportPrefixes.toSet(), staticMemberImports.toMap(), LinkedHashSet(extensionTypes))

    fun add(imports: ImportResolver) {
      typeImports.putAll(imports.typeImports)
      wildcardTypeImportPrefixes.addAll(imports.wildcardTypeImportPrefixes)
      staticMemberImports.putAll(imports.staticMemberImports)
      extensionTypes.addAll(imports.extensionTypes)
    }
  }
