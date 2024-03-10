package com.tambapps.marcel.semantic.imprt

import com.tambapps.marcel.parser.cst.imprt.*
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.imprt.ImportResolver.Companion.DEFAULT_IMPORTS
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver

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
    val typeImports = DEFAULT_IMPORTS.typeImports.toMutableMap()
    val wildcardTypeImportPrefixes = DEFAULT_IMPORTS.wildcardTypeImportPrefixes.toMutableSet()
    val staticMemberImports = DEFAULT_IMPORTS.staticMemberImports.toMutableMap()

    override fun visit(node: SimpleImportCstNode) {
      val key = node.asName ?: node.className.let {
        if (it.contains('.')) it.substring(it.lastIndexOf('.') + 1) else it
      }
      if (typeImports.containsKey(key)) {
        throw MarcelSemanticException(node.token, "An import for type $key already exists")
      }
      // TODO big problem. in a maven project, where there are multiple source files, we can't reference,
      //  classes defined in another file. We may need to use this class later in the semantic analysis (after having defined the symbols)
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