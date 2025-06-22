package com.tambapps.marcel.semantic.processor.imprt

import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.imprt.*
import com.tambapps.marcel.parser.cst.visitor.ImportCstNodeVisitor
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.type.JavaType

object ImportResolverGenerator {

  fun generate(symbolResolver: MarcelSymbolResolver, cstImports: List<ImportCstNode>): ImportResolver {
    val builder = ImportResolverBuilder(symbolResolver)
    cstImports.forEach { it.accept(builder) }
    return builder.build()
  }

  fun generateImports(
    symbolResolver: MarcelSymbolResolver,
    cstImports: List<ImportCstNode>,
    extensionImports: List<TypeCstNode> = emptyList(),
  ): MutableImportResolver {
    val builder = ImportResolverBuilder(symbolResolver)
    cstImports.forEach { it.accept(builder) }
    extensionImports.forEach { builder.visitExtensionType(it) }
    return builder.buildImports()
  }

  private class ImportResolverBuilder(
    private val symbolResolver: MarcelSymbolResolver
  ): ImportCstNodeVisitor<Unit> {
    val typeImports = mutableMapOf<String, JavaType>()
    val wildcardTypeImportPrefixes = mutableSetOf<String>()
    val staticMemberImports = mutableMapOf<String, JavaType>()
    val extensionImports = LinkedHashSet<JavaType>()

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

    fun visitExtensionType(node: TypeCstNode) {
      val type = symbolResolver.of(node.value, token = node.token)
      if (!type.isExtensionType) {
        throw MarcelSemanticException(node.token, "Type $type is not an extension")
      }
      extensionImports.add(type)
    }

    fun build() = ImportResolver(typeImports, wildcardTypeImportPrefixes, staticMemberImports, extensionImports)
    fun buildImports() = MutableImportResolver(typeImports, wildcardTypeImportPrefixes, staticMemberImports, extensionImports)

  }
}