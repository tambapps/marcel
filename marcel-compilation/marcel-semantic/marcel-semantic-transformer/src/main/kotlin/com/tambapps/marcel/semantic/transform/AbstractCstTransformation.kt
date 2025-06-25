package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.ClassCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.transform.SemanticPurpose
import com.tambapps.marcel.semantic.transform.compose.CstNodeComposer
import com.tambapps.marcel.semantic.processor.CstSymbolSemantic
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.processor.imprt.ImportResolver
import com.tambapps.marcel.semantic.processor.imprt.ImportResolverGenerator
import com.tambapps.marcel.semantic.symbol.method.MethodParameter
import com.tambapps.marcel.semantic.processor.scope.ImportScope
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.type.SourceJavaType

/**
 * Base class for CST transformations providing handy methods to handle/generate CST nodes
 */
abstract class AbstractCstTransformation : CstNodeComposer(), CstSymbolSemantic, SyntaxTreeTransformation {

  lateinit var symbolResolver: MarcelSymbolResolver
  protected lateinit var purpose: SemanticPurpose

  private var scope: ImportScope? = null

  override fun init(symbolResolver: MarcelSymbolResolver, purpose: SemanticPurpose) {
    this.symbolResolver = symbolResolver
    this.purpose = purpose
  }

  final override fun transform(javaType: SourceJavaType, node: CstNode, annotation: AnnotationNode) {
    scope = newScope(node)
    doTransform(javaType, node, annotation)
    scope = null
  }

  final override fun transform(node: AstNode, classNode: ClassNode, annotation: AnnotationNode) {
    // do nothing, as this method is for AST transformation
  }

  private fun newScope(node: CstNode): ImportScope {
    var cstNode: CstNode = node
    while (cstNode !is SourceFileCstNode) {
      if (node.parent == null) return ImportScope(symbolResolver, ImportResolver.DEFAULT_IMPORTS, null)
      cstNode = cstNode.parent!!
    }
    return ImportScope(
      symbolResolver,
      ImportResolver.DEFAULT_IMPORTS + ImportResolverGenerator.generate(symbolResolver, cstNode.imports),
      cstNode.packageName
    )
  }

  override fun toMethodParameter(
    ownerType: JavaType,
    forExtensionType: JavaType?,
    visibility: Visibility,
    isStatic: Boolean,
    parameterIndex: Int,
    methodName: String,
    node: MethodParameterCstNode
  ): MethodParameter {
    // we don't handle annotations nor defaultValue. Yup, CST transformations are limited
    val type = resolve(node.type)
    return MethodParameter(
      type, Nullness.of(type, node.isNullable), node.name, emptyList(), null
    )
  }

  abstract fun doTransform(javaType: SourceJavaType, node: CstNode, annotation: AnnotationNode)


  override fun resolve(node: TypeCstNode): JavaType = scope!!.resolveTypeOrThrow(node)

  protected fun addMethod(javaType: JavaType, classNode: ClassCstNode, methodNode: MethodCstNode) {
    // if the method already exists, the symbolResolver will throw an error
    symbolResolver.defineMethod(javaType, toJavaMethod(ownerType = javaType, node = methodNode))
    classNode.methods.add(methodNode)
  }
}
