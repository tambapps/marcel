package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.ClassCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.compose.CstNodeComposer
import com.tambapps.marcel.semantic.CstSemantic
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.imprt.ImportResolver
import com.tambapps.marcel.semantic.imprt.ImportResolverGenerator
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.scope.ImportScope
import com.tambapps.marcel.semantic.scope.Scope
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.type.SourceJavaType
import com.tambapps.marcel.semantic.visitor.ImportCstNodeConverter

/**
 * Base class for CST transformations providing handy methods to handle/generate CST nodes
 */
abstract class AbstractCstTransformation : CstNodeComposer(), CstSemantic, SyntaxTreeTransformation {

  lateinit var symbolResolver: MarcelSymbolResolver

  private var scope: ImportScope? = null

  override fun init(symbolResolver: MarcelSymbolResolver) {
    this.symbolResolver = symbolResolver
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
      if (node.parent == null) return ImportScope(symbolResolver, ImportResolver.DEFAULT_IMPORT_RESOLVER, null)
      cstNode = cstNode.parent!!
    }
    return ImportScope(
      symbolResolver,
      ImportResolverGenerator.generate(symbolResolver, cstNode.imports),
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
    return MethodParameter(
      resolve(node.type), node.name, emptyList(), null
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
