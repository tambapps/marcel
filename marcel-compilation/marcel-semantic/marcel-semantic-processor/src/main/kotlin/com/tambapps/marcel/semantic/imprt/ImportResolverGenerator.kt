package com.tambapps.marcel.semantic.imprt

import com.tambapps.marcel.parser.cst.imprt.*
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.imprt.ImportResolver.Companion.DEFAULT_IMPORTS
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.type.JavaType

object ImportResolverGenerator {

  fun generate(symbolResolver: MarcelSymbolResolver, cstImports: List<ImportCstNode>): ImportResolver {
    val builder = ImportResolverBuilder(symbolResolver)
    cstImports.forEach { it.accept(builder) }
    return builder.build()
  }

  fun generateImports(symbolResolver: MarcelSymbolResolver, cstImports: List<ImportCstNode>): ImportResolver.Imports {
    val builder = ImportResolverBuilder(symbolResolver)
    cstImports.forEach { it.accept(builder) }
    return builder.buildImports()
  }

  private class ImportResolverBuilder(
    private val symbolResolver: MarcelSymbolResolver
  ): ImportCstVisitor<Unit> {
    val typeImports = mutableMapOf<String, JavaType>()
    val wildcardTypeImportPrefixes = mutableSetOf<String>()
    val staticMemberImports = mutableMapOf<String, JavaType>()

    override fun visit(node: SimpleImportCstNode) {
      val key = node.asName ?: node.className.let {
        if (it.contains('.')) it.substring(it.lastIndexOf('.') + 1) else it
      }
      if (typeImports.containsKey(key)) {
        throw MarcelSemanticException(node.token, "An import for type $key already exists")
      }
      typeImports[key] = symbolResolver.of(node.className, token = node.token)
    }

    override fun visit(node: StaticImportCstNode) {
      val memberName = node.memberName
      if (staticMemberImports.containsKey(memberName)) {
        throw MarcelSemanticException(node.token, "An import for member $memberName already exists")
      }
      staticMemberImports[memberName] = symbolResolver.of(node.className, token = node.token)
    }

    override fun visit(node: WildcardImportCstNode) {
      if (!wildcardTypeImportPrefixes.add(node.prefix)) {
        throw MarcelSemanticException(node.token, "A wildcard import for prefix ${node.prefix} already exists")
      }
    }

    fun build() = ImportResolver(typeImports, wildcardTypeImportPrefixes, staticMemberImports)
    fun buildImports() = ImportResolver.Imports(typeImports, wildcardTypeImportPrefixes, staticMemberImports)

  }
}